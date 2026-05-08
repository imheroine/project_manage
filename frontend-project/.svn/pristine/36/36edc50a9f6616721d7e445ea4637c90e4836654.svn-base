<script setup>
// ==============================================================================
// 模块 1：核心依赖与全局配置
// ==============================================================================
import {ref, reactive, onMounted, watch, nextTick} from 'vue'
import request from '../utils/request'
import {ElMessage, ElMessageBox} from 'element-plus'
import {useUserStore} from '../store/user'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import axios from 'axios'

const userStore = useUserStore()

const API = {
    LIST: '/api/project/list',
    SAVE: '/api/project/save',
    DELETE: '/api/project/delete',
    USER_ALL: '/api/user/all',
    TASK_LIST: '/api/task/list',
    TASK_SAVE: '/api/task/save',
    TASK_DELETE: '/api/task/delete',
    GENERATE_CODE: '/api/project/generateCode'
}

// ==============================================================================
// 模块 2：全局状态管理 (State)
// ==============================================================================
const loading = ref(false)
const allProjects = ref([])       // 扁平化的所有项目数据
const currentProject = ref(null)  // 当前右侧选中的项目
const userList = ref([])          // 系统所有用户列表（用于下拉选择）
const stageTasks = ref([])        // 当前选中项目的任务列表
const searchDateRange = ref(null) // 顶部时间段筛选范围

// ==============================================================================
// 模块 3：左侧树状图逻辑 (Tree Navigation)
// ==============================================================================
const treeRef = ref(null)
const treeKey = ref(0)
const treeData = ref([])          // 格式化后的树状图结构数据
const filterText = ref('')        // 树状图搜索框绑定的值
const defaultProps = {children: 'children', label: 'label'}
const isTreeVisible = ref(localStorage.getItem('projectTreeVisible') !== 'false')

// 切换左侧树状图显示/隐藏
const toggleTree = () => {
    isTreeVisible.value = !isTreeVisible.value;
    localStorage.setItem('projectTreeVisible', isTreeVisible.value)
}

// 监听搜索框，触发过滤
watch(filterText, (val) => treeRef.value?.filter(val))
const filterNode = (value, data) => !value ? true : data.label.includes(value)

// 将扁平数据构建成按“年份”分组的树状图
const buildTree = (data) => {
    const map = {}
    data.forEach(item => {
        const year = item.projectYear || '未知'
        if (!map[year]) map[year] = []
        map[year].push(item)
    })
    const years = Object.keys(map).sort((a, b) => b - a)
    return years.map(year => ({
        id: `year_node_${year}`,
        label: `${year}年度`,
        isYear: true,
        children: map[year].sort((a, b) => (a.projectCode || '').localeCompare(b.projectCode || '')).map(p => ({
            id: `project_node_${p.id}`,
            label: p.projectName,
            isYear: false,
            type: p.projectType,
            rawData: p
        }))
    }))
}

// 点击树节点事件
const handleNodeClick = (data) => {
    if (!data.isYear) {
        currentProject.value = data.rawData
        fetchTasks(data.rawData.id) // 联动获取任务
    }
}

// ==============================================================================
// 模块 4：项目管理逻辑 (Project CRUD & Form)
// ==============================================================================
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
    id: null, projectYear: new Date().getFullYear(), projectType: '',
    projectCode: '', projectName: '', location: '',
    planStartDate: '', planEndDate: '', planDays: 0,
    actualStartDate: '', actualEndDate: '', actualDays: 0,
    managerIdsList: [], managerId: null, managerName: '',
    participantIdsList: [], participantIds: '', participants: '',
    createdBy: null
})

const rules = {
    projectYear: [{required: true, message: '必填', trigger: 'blur'}],
    projectType: [{required: true, message: '必填', trigger: 'change'}],
    projectName: [{required: true, message: '必填', trigger: 'blur'}],
    managerIdsList: [{required: true, message: '请至少选择一位负责人', trigger: 'change'}]
}

// 时间范围限制
const disabledEndDate = (time) => {
    if (!form.planStartDate) return false;
    return time.getTime() < new Date(form.planStartDate).getTime() - 8.64e7;
}
const actualDisabledEndDate = (time) => {
    if (!form.actualStartDate) return false;
    return time.getTime() < new Date(form.actualStartDate).getTime() - 8.64e7;
}

// 监听天数计算
watch([() => form.planStartDate, () => form.planEndDate], ([start, end]) => {
    form.planDays = (start && end) ? Math.ceil(Math.abs(new Date(end) - new Date(start)) / (1000 * 60 * 60 * 24)) + 1 : 0
})
watch([() => form.actualStartDate, () => form.actualEndDate], ([start, end]) => {
    form.actualDays = (start && end) ? Math.ceil(Math.abs(new Date(end) - new Date(start)) / (1000 * 60 * 60 * 24)) + 1 : 0
})

// 监听年度和类型，实时生成项目编号
watch([() => form.projectYear, () => form.projectType], async ([year, type]) => {
    if (dialogVisible.value && !form.id && year && type) {
        try {
            const res = await request.get(API.GENERATE_CODE, {params: {year, type}})
            if (res.code === 200) form.projectCode = res.data;
        } catch (error) {
            console.error('获取实时编号失败', error)
        }
    }
})

const resetForm = () => {
    if (formRef.value) formRef.value.resetFields()
    const currentUserId = userStore.userId;
    Object.assign(form, {
        id: null, projectYear: new Date().getFullYear(), projectType: '',
        projectCode: '', projectName: '', location: '',
        planStartDate: '', planEndDate: '', planDays: 0,
        actualStartDate: '', actualEndDate: '', actualDays: 0,
        participantIdsList: [], managerIdsList: [currentUserId],
        managerId: null, managerName: '', createdBy: currentUserId
    })
}

const handleAddProject = () => {
    resetForm();
    dialogTitle.value = '添加项目';
    dialogVisible.value = true
}

const handleEditProject = () => {
    resetForm()
    dialogTitle.value = '编辑项目'
    Object.assign(form, currentProject.value)
    if (currentProject.value.participantIds) form.participantIdsList = currentProject.value.participantIds.split(',').map(Number)
    if (currentProject.value.managerId) form.managerIdsList = String(currentProject.value.managerId).split(',').map(Number)
    dialogVisible.value = true
}

// 智能替补物理删除项目
const handleDeleteProject = () => {
    // 1. 安全防御：防止未选中项目时触发
    if (!currentProject.value) return;

    // 2. 弹出带有危险警告的确认框
    ElMessageBox.confirm(
            `确定要永久删除项目【${currentProject.value.projectName}】及其关联的所有任务和人员吗？此操作不可恢复！`,
            '危险操作',
            {
                confirmButtonText: '彻底删除',
                cancelButtonText: '取消',
                type: 'error' // 变成红色的危险警告图标
            }
    ).then(async () => {
        try {
            // 记录当前选中项目在总列表中的索引位置
            const currentIndex = allProjects.value.findIndex(p => String(p.id) === String(currentProject.value.id));

            // 发起物理删除请求
            const res = await request.delete(API.DELETE, { params: { id: currentProject.value.id } });

            if (res.code === 200) {
                ElMessage.success('项目及其关联数据已彻底抹除');

                // 重新拉取数据库的最新项目列表
                await fetchData();

                // 替补选中逻辑
                if (allProjects.value.length > 0) {
                    let newIndex = currentIndex >= allProjects.value.length ? allProjects.value.length - 1 : currentIndex;
                    const nextProject = allProjects.value[newIndex];
                    currentProject.value = nextProject;

                    fetchTasks(nextProject.id);

                    // 使用 setTimeout 给 DOM 渲染留出缓冲时间，防止高亮失效
                    setTimeout(() => {
                        if (treeRef.value) {
                            treeRef.value.setCurrentKey(`project_node_${nextProject.id}`);
                        }
                    }, 100);
                } else {
                    currentProject.value = null;
                    stageTasks.value = [];
                }
            } else {
                ElMessage.error(res.message || '删除失败');
            }
        } catch (e) {
            console.error("删除报错:", e);
            ElMessage.error('服务器异常，删除失败');
        }
    }).catch(() => {
        // 必须捕获 catch！否则点击“取消”后，Promise 异常会导致按钮永久失去交互！
        ElMessage.info('已取消删除操作');
    });
}

// 核心功能：提交表单并智能选中新项目
const submitForm = async () => {
    if (!formRef.value) return
    await formRef.value.validate(async (valid) => {
        if (valid) {
            submitLoading.value = true
            try {
                // 1. 组装人员 ID 和名称
                const selectedManagers = userList.value.filter(u => form.managerIdsList.includes(u.id))
                form.managerId = selectedManagers.map(u => u.id).join(',')
                form.managerName = selectedManagers.map(u => u.realName).join(',')

                const selectedParticipants = userList.value.filter(u => form.participantIdsList.includes(u.id))
                form.participantIds = selectedParticipants.map(u => u.id).join(',')
                form.participants = selectedParticipants.map(u => u.realName).join(',')

                // 2. 提前记录当前操作是新增还是编辑，以及关键属性（用于防空窗兜底）
                const isNew = !form.id;
                const savedCode = form.projectCode;
                const savedName = form.projectName;

                // 3. 发起保存请求
                const res = await request.post(API.SAVE, form)

                if (res.code === 200) {
                    ElMessage.success(res.message || '操作成功')
                    dialogVisible.value = false

                    const savedProject = res.data; // 尝试获取后端返回的对象

                    // 重新拉取最新数据，确保库里的新项目已经加载到 allProjects 里
                    await fetchData()

                    // 智能选中逻辑
                    let targetId = null;

                    if (savedProject && savedProject.id) {
                        // 保障1：如果后端完美回填了 ID，直接用
                        targetId = savedProject.id;
                    } else if (isNew) {
                        // 保障2：如果后端没返回 ID，通过刚才填写的“编号”或“名称”在最新列表里精准捕获！
                        const matchedProject = allProjects.value.find(p => p.projectCode === savedCode || p.projectName === savedName);
                        if (matchedProject) {
                            targetId = matchedProject.id;
                        } else if (allProjects.value.length > 0) {
                            // 保障3：如果名称也匹配不上，直接强行选中新列表的第一个（通常也就是最新添加的）
                            targetId = allProjects.value[0].id;
                        }
                    } else {
                        // 保障4：如果是编辑操作，直接沿用表单里的老 ID
                        targetId = form.id;
                    }

                    // 最终渲染右侧详情与左侧高亮
                    if (targetId) {
                        const targetProject = allProjects.value.find(p => String(p.id) === String(targetId));
                        if (targetProject) {
                            currentProject.value = targetProject; // 右侧界面立刻显示
                            fetchTasks(targetId); // 自动去查它的任务

                            // 延迟高亮：fetchData 改变了 treeKey，树组件正在销毁重建
                            // 给 100ms 延迟，确保新生成的 DOM 彻底挂载完毕后再去高亮节点
                            setTimeout(() => {
                                if (treeRef.value) {
                                    treeRef.value.setCurrentKey(`project_node_${targetId}`);
                                }
                            }, 100);
                        }
                    }
                } else {
                    ElMessage.error(res.message || '保存失败')
                }
            } catch (e) {
                ElMessage.error('网络异常，保存失败')
            } finally {
                submitLoading.value = false
            }
        }
    })
}
// ==============================================================================
// 模块 5：阶段任务管理 (Task CRUD & Form)
// ==============================================================================
const taskDialogVisible = ref(false)
const taskDialogTitle = ref('')
const taskSubmitLoading = ref(false)
const taskFormRef = ref(null)

const taskForm = reactive({
    id: null, projectId: null, stageDictCode: '', taskName: '',
    responsibleIdsList: [], responsibleId: '', responsiblePerson: '',
    participantIdsList: [], participantIds: '', participants: '',
    planStartDate: '', planEndDate: '', planWorkload: 0,
    actualStartDate: '', actualEndDate: '', actualWorkload: 0,
    statusDictCode: '未开始', issues: '',
    rectificationMeasures: '', remarks: '', attachmentUrls: ''
})

const taskRules = {
    stageDictCode: [{required: true, message: '请选择阶段', trigger: 'change'}],
    taskName: [{required: true, message: '请输入任务名称', trigger: 'blur'}],
    statusDictCode: [{required: true, message: '请选择状态', trigger: 'change'}],
    responsibleIdsList: [{required: true, message: '请选择负责人', trigger: 'change'}]
}

const taskDisabledEndDate = (time) => taskForm.planStartDate ? time.getTime() < new Date(taskForm.planStartDate).getTime() - 8.64e7 : false
const taskActualDisabledEndDate = (time) => taskForm.actualStartDate ? time.getTime() < new Date(taskForm.actualStartDate).getTime() - 8.64e7 : false

// 任务列表拉取
const fetchTasks = async (projectId) => {
    if (!projectId) return;
    try {
        const res = await request.get(API.TASK_LIST, {params: {projectId}})
        stageTasks.value = res.data || []
    } catch (error) {
        console.error('获取任务失败')
    }
}

const resetTaskForm = () => {
    if (taskFormRef.value) taskFormRef.value.resetFields()
    Object.assign(taskForm, {
        id: null,
        projectId: currentProject.value?.id,
        stageDictCode: '',
        taskName: '',
        planStartDate: '',
        planEndDate: '',
        planWorkload: 0,
        actualStartDate: '',
        actualEndDate: '',
        actualWorkload: 0,
        responsibleIdsList: [],
        responsibleId: '',
        responsiblePerson: '',
        participantIdsList: [],
        participantIds: '',
        participants: '',
        statusDictCode: '未开始',
        issues: '',
        rectificationMeasures: '',
        remarks: '',
        attachmentUrls: ''
    })
}

const handleAddTask = () => {
    resetTaskForm();
    taskDialogTitle.value = '新增阶段任务';
    taskDialogVisible.value = true
}

const handleEditTask = (row) => {
    resetTaskForm();
    taskDialogTitle.value = '编辑阶段任务';
    Object.assign(taskForm, row);
    if (row.responsibleId) taskForm.responsibleIdsList = String(row.responsibleId).split(',').map(Number)
    if (row.participantIds) taskForm.participantIdsList = String(row.participantIds).split(',').map(Number)
    taskDialogVisible.value = true;
}

const handleDeleteTask = (id) => {
    ElMessageBox.confirm('确定删除该任务吗？', '警告', {type: 'warning'}).then(async () => {
        try {
            await request.delete(`${API.TASK_DELETE}?id=${id}`)
            ElMessage.success('删除成功');
            fetchTasks(currentProject.value.id);
        } catch (e) {
            ElMessage.error('删除失败')
        }
    }).catch(() => {
    })
}

const submitTaskForm = async () => {
    if (!taskFormRef.value) return
    await taskFormRef.value.validate(async (valid) => {
        if (valid) {
            taskSubmitLoading.value = true
            try {
                const selectedRes = userList.value.filter(u => taskForm.responsibleIdsList.includes(u.id))
                taskForm.responsibleId = selectedRes.map(u => u.id).join(',')
                taskForm.responsiblePerson = selectedRes.map(u => u.realName).join(',')

                const selectedPart = userList.value.filter(u => taskForm.participantIdsList.includes(u.id))
                taskForm.participantIds = selectedPart.map(u => u.id).join(',')
                taskForm.participants = selectedPart.map(u => u.realName).join(',')

                await request.post(API.TASK_SAVE, taskForm)
                ElMessage.success('保存成功')
                taskDialogVisible.value = false
                fetchTasks(currentProject.value.id)
            } catch (e) {
                ElMessage.error('保存失败')
            } finally {
                taskSubmitLoading.value = false
            }
        }
    })
}

// 附件上传与移除
const handleUploadSuccess = (res) => {
    if (res.code === 200) {
        taskForm.attachmentUrls = res.data;
        ElMessage.success('附件上传成功');
    } else {
        ElMessage.error(res.message || '上传失败');
    }
}
const handleRemoveAttachment = () => {
    taskForm.attachmentUrls = '';
}

// ==============================================================================
// 模块 6：导入导出逻辑 (Import & Export)
// ==============================================================================
const exportLoading = ref(false)
const importLoading = ref(false)

const handleExport = async () => {
    if (!currentProject.value) {
        ElMessage.warning('请先在左侧选择一个要导出的项目！');
        return;
    }
    exportLoading.value = true;
    try {
        ElMessage.info('正在生成精美报表，请稍候...');
        const res = await axios.get(API.LIST.replace('/list', '/export'), {
            params: {projectId: currentProject.value.id},
            responseType: 'blob',
            headers: {Authorization: userStore.token}
        });

        const blob = new Blob([res.data], {type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'});
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `${currentProject.value.projectName}_项目明细_${new Date().getTime()}.xlsx`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);

        ElMessage.success('导出成功');
    } catch (error) {
        ElMessage.error('导出失败，请重试');
    } finally {
        exportLoading.value = false;
    }
}

const beforeImport = (file) => {
    const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' || file.type === 'application/vnd.ms-excel'
    if (!isExcel) {
        ElMessage.error('上传文件只能是 xlsx/xls 格式!')
        return false
    }
    importLoading.value = true
    return true
}

// 导入成功回调 (加入智能替补与高亮锁定逻辑)
const handleImportSuccess = async (res) => {
    importLoading.value = false;

    if (res.code === 200) {
        ElMessage.success(res.message || '导入成功');

        // 1. 拿到后端刚刚返回的新项目 ID
        const importedProjectId = res.data;

        // 2. 等待最新的项目列表从数据库拉取完毕
        await fetchData();

        // 3. 智能选中逻辑
        if (importedProjectId) {
            // 精确在列表中找到刚才导入的项目
            const targetProject = allProjects.value.find(p => String(p.id) === String(importedProjectId));

            if (targetProject) {
                // 将右侧详情强制设为这个新项目
                currentProject.value = targetProject;

                // 拉取该项目下的所有任务 (刚才一起导入进去的那些阶段任务)
                fetchTasks(targetProject.id);

                // 使用 setTimeout 给 DOM 渲染留出缓冲时间，让左侧树状图同步高亮
                setTimeout(() => {
                    if (treeRef.value) {
                        treeRef.value.setCurrentKey(`project_node_${targetProject.id}`);
                    }
                }, 100);
            }
        }
    } else {
        ElMessage.error(res.message || '导入失败');
    }
}

const handleImportError = () => {
    importLoading.value = false
    ElMessage.error('网络或服务器异常，导入失败')
}

// ==============================================================================
// 模块 7：生命周期与数据初始化 (Lifecycle & Init)
// ==============================================================================
const formatDate = (dateStr) => dateStr ? dateStr.split('T')[0] : '-'

const fetchUserList = async () => {
    try {
        const res = await request.get(API.USER_ALL)
        userList.value = res.data || []
    } catch (error) {
        console.error('获取用户失败')
    }
}

const fetchData = async () => {
    loading.value = true
    try {
        const userId = userStore.userId;
        if (!userId) {
            ElMessage.error('未获取到用户信息，请重新登录');
            return;
        }

        const params = {userId: userId}
        if (searchDateRange.value && searchDateRange.value.length === 2) {
            params.startDate = searchDateRange.value[0]
            params.endDate = searchDateRange.value[1]
        }

        const res = await request.get(API.LIST, {params: params})

        allProjects.value = [...(res.data || [])];
        treeData.value = buildTree(allProjects.value);
        treeKey.value += 1;

        // 如果之前选中了项目，刷新后保持选中并更新数据
        if (currentProject.value) {
            const updated = allProjects.value.find(p => String(p.id) === String(currentProject.value.id))
            currentProject.value = updated || null
        }
    } catch (error) {
        ElMessage.error('获取项目列表失败')
    } finally {
        loading.value = false
    }
}

onMounted(() => {
    fetchData();
    fetchUserList();
})
</script>

<template>
    <el-config-provider :locale="zhCn">
        <el-container class="project-layout">
            <el-aside :width="isTreeVisible ? '280px' : '0px'" class="tree-aside">
                <div class="tree-header" v-show="isTreeVisible">
                    <span>项目导航树</span>
                </div>
                <div class="tree-body" v-show="isTreeVisible">
                    <el-input v-model="filterText" placeholder="搜索项目名称" clearable class="search-input"/>
                    <el-tree
                            ref="treeRef"
                            :key="treeKey"
                            :data="treeData"
                            :props="defaultProps"
                            default-expand-all
                            highlight-current
                            :filter-node-method="filterNode"
                            @node-click="handleNodeClick"
                            node-key="id"
                    >
                        <template #default="{ node, data }">
              <span class="custom-tree-node">
                <span v-if="data.isYear" class="icon">📁</span>
                <span v-else-if="data.type === '研发项目'" class="icon">💻</span>
                <span v-else-if="data.type === '实施项目'" class="icon">⚙️</span>
                <span v-else-if="data.type === '维护项目'" class="icon">🔧</span>
                <span v-else class="icon">📄</span>
                <span class="label" :title="node.label">{{ node.label }}</span>
              </span>
                        </template>
                    </el-tree>
                </div>
            </el-aside>

            <el-main class="main-workspace">
                <div class="toolbar">
                    <div class="toolbar-left" style="display: flex; align-items: center;">
                        <el-button @click="toggleTree" :icon="isTreeVisible ? 'Fold' : 'Expand'">
                            {{ isTreeVisible ? '隐藏导航' : '展开导航' }}
                        </el-button>
                        <el-date-picker
                                v-model="searchDateRange"
                                type="daterange"
                                range-separator="至"
                                start-placeholder="计划开始日期"
                                end-placeholder="计划结束日期"
                                value-format="YYYY-MM-DD"
                                @change="fetchData"
                                style="margin-left: 15px; width: 260px;"
                                clearable
                        />
                    </div>
                    <div class="toolbar-right" style="display: flex; gap: 10px;">
                        <el-button type="info" icon="Download" @click="handleExport" :loading="exportLoading">导出
                        </el-button>

                        <el-upload
                                action="/api/project/import"
                                :headers="{ Authorization: userStore.token }"
                                :data="{ userId: userStore.userId }"
                                :show-file-list="false"
                                :on-success="handleImportSuccess"
                                :on-error="handleImportError"
                                :before-upload="beforeImport"
                                accept=".xlsx, .xls"
                        >
                            <el-button type="info" icon="Upload" :loading="importLoading">导入</el-button>
                        </el-upload>

                        <el-button type="success" icon="Plus" @click="handleAddProject">添加项目</el-button>
                        <el-button type="warning" icon="Edit" :disabled="!currentProject" @click="handleEditProject">
                            编辑项目
                        </el-button>
                        <el-button type="danger" icon="Delete" :disabled="!currentProject" @click="handleDeleteProject">
                            删除项目
                        </el-button>
                    </div>

                </div>

                <div class="workspace-content" v-loading="loading">
                    <template v-if="currentProject">
                        <div class="info-section">
                            <div class="section-title">项目整体情况</div>
                            <el-descriptions border :column="2" size="default" class="fixed-table"
                                             :label-style="{ width: '120px', 'justify-content': 'center' }"
                                             :content-style="{ 'word-break': 'break-all' }">
                                <el-descriptions-item label="年度">{{ currentProject.projectYear }}</el-descriptions-item>
                                <el-descriptions-item label="项目编号">{{
                                        currentProject.projectCode
                                    }}
                                </el-descriptions-item>
                                <el-descriptions-item label="项目名称"><span
                                        class="project-name-text">{{ currentProject.projectName }}</span>
                                </el-descriptions-item>
                                <el-descriptions-item label="项目类型">
                                    <el-tag size="small">{{ currentProject.projectType }}</el-tag>
                                </el-descriptions-item>
                                <el-descriptions-item label="负责人">{{
                                        currentProject.managerName || '-'
                                    }}
                                </el-descriptions-item>
                                <el-descriptions-item label="项目地点">{{
                                        currentProject.location || '-'
                                    }}
                                </el-descriptions-item>
                                <el-descriptions-item label="计划开始">{{
                                        formatDate(currentProject.planStartDate)
                                    }}
                                </el-descriptions-item>
                                <el-descriptions-item label="计划结束">{{
                                        formatDate(currentProject.planEndDate)
                                    }}
                                </el-descriptions-item>
                                <el-descriptions-item label="实际开始">
                                    <span :style="{ color: currentProject.actualStartDate ? '#67c23a' : '#909399' }">
                                        {{ formatDate(currentProject.actualStartDate) || '尚未开始' }}
                                    </span>
                                </el-descriptions-item>
                                <el-descriptions-item label="实际结束">
                                    <span :style="{ color: currentProject.actualEndDate ? '#67c23a' : '#909399' }">
                                        {{ formatDate(currentProject.actualEndDate) || '尚未结束' }}
                                    </span>
                                </el-descriptions-item>
                                <el-descriptions-item label="计划天数"><span v-if="currentProject.planDays"
                                                                         class="days-highlight">{{
                                        currentProject.planDays
                                    }} 天</span><span v-else>-</span></el-descriptions-item>
                                <el-descriptions-item label="实际天数"><span v-if="currentProject.actualDays"
                                                                         class="days-highlight">{{
                                        currentProject.actualDays
                                    }} 天</span><span v-else>-</span></el-descriptions-item>
                                <el-descriptions-item label="参与人员" :span="2">{{
                                        currentProject.participants || '-'
                                    }}
                                </el-descriptions-item>

                            </el-descriptions>
                        </div>

                        <div class="info-section">
                            <div class="section-title" style="display: flex; justify-content: space-between;">
                                <span>项目阶段与任务</span>
                                <el-button type="primary" size="small" icon="Plus" @click="handleAddTask">添加阶段/任务
                                </el-button>
                            </div>
                            <el-table :data="stageTasks" border size="small" style="width: 100%"
                                      empty-text="该项目暂无任务，请点击右上方添加">
                                <el-table-column prop="stageDictCode" label="阶段名称" width="120"/>
                                <el-table-column prop="taskName" label="任务名称" min-width="150" show-overflow-tooltip/>
                                <el-table-column prop="statusDictCode" label="状态" width="80" align="center">
                                    <template #default="scope">
                                        <el-tag :type="scope.row.statusDictCode === '已完成' ? 'success' : (scope.row.statusDictCode === '进行中' ? 'warning' : 'info')">
                                            {{ scope.row.statusDictCode || '未开始' }}
                                        </el-tag>
                                    </template>
                                </el-table-column>
                                <el-table-column prop="responsiblePerson" label="负责人" min-width="120"
                                                 show-overflow-tooltip/>
                                <el-table-column prop="participants" label="参与人" min-width="120" show-overflow-tooltip/>
                                <el-table-column label="计划开始" width="100">
                                    <template #default="scope">{{ formatDate(scope.row.planStartDate) }}</template>
                                </el-table-column>
                                <el-table-column label="计划结束" width="100">
                                    <template #default="scope">{{ formatDate(scope.row.planEndDate) }}</template>
                                </el-table-column>
                                <el-table-column label="实际开始" width="100">
                                    <template #default="scope">{{ formatDate(scope.row.actualStartDate) }}</template>
                                </el-table-column>
                                <el-table-column label="实际结束" width="100">
                                    <template #default="scope">{{ formatDate(scope.row.actualEndDate) }}</template>
                                </el-table-column>
                                <el-table-column prop="planWorkload" label="计划工作量" width="90"/>
                                <el-table-column prop="actualWorkload" label="实际工作量" width="90"/>
                                <el-table-column prop="issues" label="存在问题" min-width="120" show-overflow-tooltip>
                                    <template #default="scope">{{ scope.row.issues || '-' }}</template>
                                </el-table-column>
                                <el-table-column prop="rectificationMeasures" label="整改措施" min-width="120"
                                                 show-overflow-tooltip>
                                    <template #default="scope">{{ scope.row.rectificationMeasures || '-' }}</template>
                                </el-table-column>
                                <el-table-column prop="remarks" label="备注" min-width="100" show-overflow-tooltip>
                                    <template #default="scope">{{ scope.row.remarks || '-' }}</template>
                                </el-table-column>
                                <el-table-column label="附件" width="100" align="center" fixed="right">
                                    <template #default="scope">
                                        <el-link v-if="scope.row.attachmentUrls" type="primary"
                                                 :href="'http://localhost:8080' + scope.row.attachmentUrls"
                                                 target="_blank" :underline="false">下载附件
                                        </el-link>
                                        <span v-else>-</span>
                                    </template>
                                </el-table-column>
                                <el-table-column label="操作" width="120" fixed="right" align="center">
                                    <template #default="scope">
                                        <el-button size="small" type="primary" link @click="handleEditTask(scope.row)">
                                            编辑
                                        </el-button>
                                        <el-button size="small" type="danger" link
                                                   @click="handleDeleteTask(scope.row.id)">删除
                                        </el-button>
                                    </template>
                                </el-table-column>
                            </el-table>
                        </div>
                    </template>
                    <el-empty v-else description="请从左侧项目树中选择一个项目查看"/>
                </div>
            </el-main>

            <el-dialog :title="dialogTitle" v-model="dialogVisible" width="800px" destroy-on-close>
                <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="project-form">
                    <el-row :gutter="24">
                        <el-col :span="12">
                            <el-form-item label="所属年度" prop="projectYear">
                                <el-input-number v-model="form.projectYear" :min="2000" :max="2100"
                                                 style="width: 100%;"/>
                            </el-form-item>
                        </el-col>
                        <el-col :span="12">
                            <el-form-item label="项目类型" prop="projectType">
                                <el-select v-model="form.projectType" placeholder="请选择类型" style="width: 100%;">
                                    <el-option label="研发项目" value="研发项目"/>
                                    <el-option label="实施项目" value="实施项目"/>
                                    <el-option label="维护项目" value="维护项目"/>
                                </el-select>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-row :gutter="24">
                        <el-col :span="12">
                            <el-form-item label="项目名称" prop="projectName">
                                <el-input v-model="form.projectName" placeholder="请输入项目名称"/>
                            </el-form-item>
                        </el-col>
                        <el-col :span="12">
                            <el-form-item label="项目编号">
                                <el-input v-model="form.projectCode" disabled placeholder="选择年度和类型后自动生成"/>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-row :gutter="24">
                        <el-col :span="12">
                            <el-form-item label="项目负责人" prop="managerIdsList">
                                <el-select v-model="form.managerIdsList" multiple filterable placeholder="可选择多名负责人"
                                           style="width: 100%;">
                                    <el-option v-for="user in userList" :key="user.id" :label="user.realName"
                                               :value="user.id"/>
                                </el-select>
                            </el-form-item>
                        </el-col>
                        <el-col :span="12">
                            <el-form-item label="项目地点" prop="location">
                                <el-input v-model="form.location" placeholder="请输入项目地点"/>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-form-item label="项目参与人" prop="participantIdsList">
                        <el-select v-model="form.participantIdsList" multiple filterable placeholder="请选择参与人员"
                                   style="width: 100%;">
                            <el-option v-for="user in userList" :key="user.id" :label="user.realName" :value="user.id"/>
                        </el-select>
                    </el-form-item>
                    <el-row :gutter="24" class="date-row">
                        <el-col :span="10">
                            <el-form-item label="计划开始" prop="planStartDate">
                                <el-date-picker v-model="form.planStartDate" type="date" value-format="YYYY-MM-DD"
                                                style="width: 100%;"/>
                            </el-form-item>
                        </el-col>
                        <el-col :span="10">
                            <el-form-item label="计划结束" prop="planEndDate">
                                <el-date-picker v-model="form.planEndDate" type="date" value-format="YYYY-MM-DD"
                                                :disabled-date="disabledEndDate" style="width: 100%;"/>
                            </el-form-item>
                        </el-col>
                        <el-col :span="4">
                            <el-form-item label-width="0">
                                <div class="plan-days-tag">共 {{ form.planDays || 0 }} 天</div>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-row :gutter="24" class="date-row">
                        <el-col :span="10">
                            <el-form-item label="实际开始" prop="actualStartDate">
                                <el-date-picker v-model="form.actualStartDate" type="date" value-format="YYYY-MM-DD"
                                                style="width: 100%;"/>
                            </el-form-item>
                        </el-col>
                        <el-col :span="10">
                            <el-form-item label="实际结束" prop="actualEndDate">
                                <el-date-picker v-model="form.actualEndDate" type="date" value-format="YYYY-MM-DD"
                                                :disabled-date="actualDisabledEndDate" style="width: 100%;"/>
                            </el-form-item>
                        </el-col>
                        <el-col :span="4">
                            <el-form-item label-width="0">
                                <div class="plan-days-tag">共 {{ form.actualDays || 0 }} 天</div>
                            </el-form-item>
                        </el-col>
                    </el-row>
                </el-form>
                <template #footer>
                    <el-button @click="dialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="submitForm" :loading="submitLoading">确定保存</el-button>
                </template>
            </el-dialog>

            <el-dialog :title="taskDialogTitle" v-model="taskDialogVisible" width="800px" destroy-on-close>
                <el-form ref="taskFormRef" :model="taskForm" :rules="taskRules" label-width="110px">
                    <el-row :gutter="24">
                        <el-col :span="12">
                            <el-form-item label="阶段名称" prop="stageDictCode">
                                <el-select v-model="taskForm.stageDictCode" placeholder="请选择阶段" style="width: 100%;">
                                    <el-option label="需求分析阶段" value="需求分析阶段"/>
                                    <el-option label="设计开发阶段" value="设计开发阶段"/>
                                    <el-option label="测试验收阶段" value="测试验收阶段"/>
                                </el-select>
                            </el-form-item>
                        </el-col>
                        <el-col :span="12">
                            <el-form-item label="任务状态" prop="statusDictCode">
                                <el-select v-model="taskForm.statusDictCode" placeholder="请选择状态" style="width: 100%;">
                                    <el-option label="未开始" value="未开始"/>
                                    <el-option label="进行中" value="进行中"/>
                                    <el-option label="已完成" value="已完成"/>
                                </el-select>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-form-item label="任务名称" prop="taskName">
                        <el-input v-model="taskForm.taskName" placeholder="请输入任务名称"/>
                    </el-form-item>
                    <el-row :gutter="24">
                        <el-col :span="12">
                            <el-form-item label="任务负责人" prop="responsibleIdsList">
                                <el-select v-model="taskForm.responsibleIdsList" multiple filterable
                                           placeholder="请选择负责人" style="width: 100%;">
                                    <el-option v-for="user in userList" :key="user.id" :label="user.realName"
                                               :value="user.id"/>
                                </el-select>
                            </el-form-item>
                        </el-col>
                        <el-col :span="12">
                            <el-form-item label="任务参与人" prop="participantIdsList">
                                <el-select v-model="taskForm.participantIdsList" multiple filterable
                                           placeholder="请选择参与人" style="width: 100%;">
                                    <el-option v-for="user in userList" :key="user.id" :label="user.realName"
                                               :value="user.id"/>
                                </el-select>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-row :gutter="24">
                        <el-col :span="12">
                            <el-form-item label="计划开始" prop="planStartDate">
                                <el-date-picker v-model="taskForm.planStartDate" type="date" value-format="YYYY-MM-DD"
                                                style="width: 100%;"/>
                            </el-form-item>
                        </el-col>
                        <el-col :span="12">
                            <el-form-item label="计划结束" prop="planEndDate">
                                <el-date-picker v-model="taskForm.planEndDate" type="date" value-format="YYYY-MM-DD"
                                                :disabled-date="taskDisabledEndDate" style="width: 100%;"/>
                            </el-form-item>
                        </el-col>
                        <el-col :span="12">
                            <el-form-item label="实际开始" prop="actualStartDate">
                                <el-date-picker v-model="taskForm.actualStartDate" type="date" value-format="YYYY-MM-DD"
                                                style="width: 100%;"/>
                            </el-form-item>
                        </el-col>
                        <el-col :span="12">
                            <el-form-item label="实际结束" prop="actualEndDate">
                                <el-date-picker v-model="taskForm.actualEndDate" type="date" value-format="YYYY-MM-DD"
                                                :disabled-date="taskActualDisabledEndDate" style="width: 100%;"/>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-row :gutter="24">
                        <el-col :span="12">
                            <el-form-item label="计划工作量" prop="planWorkload">
                                <el-input-number v-model="taskForm.planWorkload" :min="0" :precision="2" :step="0.5"
                                                 style="width: 100%;"/>
                            </el-form-item>
                        </el-col>
                        <el-col :span="12">
                            <el-form-item label="实际工作量" prop="actualWorkload">
                                <el-input-number v-model="taskForm.actualWorkload" :min="0" :precision="2" :step="0.5"
                                                 style="width: 100%;"/>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-form-item label="存在问题" prop="issues">
                        <el-input v-model="taskForm.issues" type="textarea" :rows="2" placeholder="记录存在的问题"/>
                    </el-form-item>
                    <el-form-item label="整改措施" prop="rectificationMeasures">
                        <el-input v-model="taskForm.rectificationMeasures" type="textarea" :rows="2"
                                  placeholder="填写整改措施"/>
                    </el-form-item>
                    <el-form-item label="备注" prop="remarks">
                        <el-input v-model="taskForm.remarks" type="textarea" :rows="1"/>
                    </el-form-item>
                    <el-form-item label="任务附件" prop="attachmentUrls">
                        <el-upload
                                class="upload-demo"
                                action="/api/file/upload"
                                :headers="{ Authorization: userStore.token }"
                                :on-success="handleUploadSuccess"
                                :on-remove="handleRemoveAttachment"
                                :limit="1"
                                :file-list="taskForm.attachmentUrls ? [{name: '已上传附件', url: taskForm.attachmentUrls}] : []"
                        >
                            <el-button type="primary" plain size="small">点击上传</el-button>
                            <template #tip>
                                <div class="el-upload__tip">最多支持上传1个文件</div>
                            </template>
                        </el-upload>
                    </el-form-item>
                </el-form>
                <template #footer>
                    <el-button @click="taskDialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="submitTaskForm" :loading="taskSubmitLoading">确定保存</el-button>
                </template>
            </el-dialog>
        </el-container>
    </el-config-provider>
</template>

<style scoped>
.project-layout {
    height: calc(100vh - 84px);
    background-color: #f5f7fa;
    padding: 15px;
    gap: 15px;
}

.tree-aside {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
    transition: width 0.3s;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.tree-header {
    padding: 16px;
    font-size: 16px;
    font-weight: 600;
    border-bottom: 1px solid #f0f0f0;
    color: #303133;
}

.tree-body {
    padding: 12px;
    flex: 1;
    overflow-y: auto;
}

.search-input {
    margin-bottom: 12px;
}

.custom-tree-node {
    display: flex;
    align-items: center;
    font-size: 14px;
}

.custom-tree-node .icon {
    margin-right: 8px;
    font-size: 16px;
}

.main-workspace {
    background: #fff;
    border-radius: 8px;
    padding: 0;
    display: flex;
    flex-direction: column;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
    overflow: hidden;
}

.toolbar {
    padding: 12px 20px;
    border-bottom: 1px solid #f0f0f0;
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: #fff;
}

.workspace-content {
    flex: 1;
    padding: 24px;
    overflow-y: auto;
}

.info-section {
    margin-bottom: 30px;
}

.section-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 20px;
    padding-left: 12px;
    border-left: 4px solid #409EFF;
    line-height: 1;
}

.project-name-text {
    font-weight: 600;
    color: #409EFF;
    font-size: 15px;
}

.days-highlight {
    font-weight: 600;
    color: #e6a23c;
}

.plan-days-tag {
    background: #fdf6ec;
    color: #e6a23c;
    padding: 0 15px;
    border-radius: 4px;
    height: 32px;
    line-height: 32px;
    border: 1px solid #faecd8;
    text-align: center;
    font-weight: bold;
    white-space: nowrap;
}

.date-row {
    background: #fcfcfc;
    padding: 15px 0 5px 0;
    border-radius: 4px;
    border: 1px dashed #dcdfe6;
    margin-left: 0 !important;
    margin-right: 0 !important;
    margin-bottom: 18px;
}
/* ================= 固定表格列宽 ================= */
.fixed-table ::v-deep(table) {
    table-layout: fixed !important;
    width: 100% !important;
}
.fixed-table ::v-deep(.el-descriptions__content) {
    word-break: break-all; /* 允许在单词内换行 */
    white-space: normal;   /* 正常换行 */
}
</style>