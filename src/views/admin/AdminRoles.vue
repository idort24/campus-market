<script setup>
// ============================================================
// 角色管理（RBAC 核心页）
// ------------------------------------------------------------
// 角色不是写死的：可新增、编辑、删除、启停，权限按角色动态分配。
// 数据源走 api/admin.js（mock 时落 localStorage，刷新不丢；
// 关掉 mock 则走真实后端 /roles 系列接口）。
// ============================================================
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { roleApi } from '../../api/admin.js'

const roles = ref([])
const loading = ref(false)
const permTree = ref([])

/* ---------------- 列表 ---------------- */

async function loadRoles() {
  loading.value = true
  try {
    const res = await roleApi.list()
    const list = Array.isArray(res) ? res : res.records || []
    // 并行补每个角色已分配的权限，列表里要显示出来
    await Promise.all(
      list.map(async (r) => {
        try {
          r.permIds = await roleApi.rolePerms(r.id)
        } catch {
          r.permIds = []
        }
      })
    )
    roles.value = list
  } finally {
    loading.value = false
  }
}

function permLabel(row) {
  if (row.roleCode === 'ADMIN') return ['全部权限']
  return roleApi.permNames(row.permIds || [])
}

/* ---------------- 分配权限 ---------------- */

const assignVisible = ref(false)
const assignRow = ref(null)
const checkedKeys = ref([])
const treeRef = ref(null)

async function openAssign(row) {
  assignRow.value = row
  if (!permTree.value.length) permTree.value = await roleApi.permissionTree()
  checkedKeys.value = Array.isArray(row.permIds) ? row.permIds : await roleApi.rolePerms(row.id)
  assignVisible.value = true
}

async function confirmAssign() {
  // 全选 + 半选父节点一起落库，否则父菜单取不到
  const leafChecked = treeRef.value.getCheckedKeys()
  const halfChecked = treeRef.value.getHalfCheckedKeys()
  const allKeys = [...leafChecked, ...halfChecked]
  try {
    await roleApi.assign(assignRow.value.id, allKeys)
    ElMessage.success(`已更新角色「${assignRow.value.roleName}」的权限`)
    assignVisible.value = false
    await loadRoles()
  } catch (e) {
    ElMessage.error(e.message || '分配失败')
  }
}

/* ---------------- 新增 / 编辑 ---------------- */

const formVisible = ref(false)
const formMode = ref('create')
const formRef = ref(null)
const saving = ref(false)
const form = ref({ id: null, roleCode: '', roleName: '', roleDesc: '', status: 1 })
const formTreeRef = ref(null)
const formPermKeys = ref([])

const rules = {
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[A-Z][A-Z0-9_]{1,19}$/, message: '大写字母开头，可含数字和下划线，2–20 位', trigger: 'blur' },
  ],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
}

async function openCreate() {
  formMode.value = 'create'
  form.value = { id: null, roleCode: '', roleName: '', roleDesc: '', status: 1 }
  formPermKeys.value = []
  if (!permTree.value.length) permTree.value = await roleApi.permissionTree()
  formVisible.value = true
}

function openEdit(row) {
  formMode.value = 'edit'
  form.value = {
    id: row.id,
    roleCode: row.roleCode,
    roleName: row.roleName,
    roleDesc: row.roleDesc || '',
    status: row.status,
  }
  formVisible.value = true
}

async function submitForm() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  saving.value = true
  try {
    if (formMode.value === 'create') {
      const permIds = formTreeRef.value
        ? [...formTreeRef.value.getCheckedKeys(), ...formTreeRef.value.getHalfCheckedKeys()]
        : []
      await roleApi.create({ ...form.value, permIds })
      ElMessage.success(`角色「${form.value.roleName}」已创建`)
    } else {
      await roleApi.update(form.value.id, {
        roleName: form.value.roleName,
        roleDesc: form.value.roleDesc,
        status: form.value.status,
      })
      ElMessage.success('角色已更新')
    }
    formVisible.value = false
    await loadRoles()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function removeRole(row) {
  try {
    await ElMessageBox.confirm(
      `确定删除角色「${row.roleName}」？该角色的权限分配会一并清除。`,
      '删除确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await roleApi.remove(row.id)
    ElMessage.success('已删除')
    await loadRoles()
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

async function changeStatus(row, val) {
  try {
    await roleApi.toggleStatus(row.id, val)
    // 开关是单向绑定，成功后要把行数据回写，否则 UI 会弹回旧值
    row.status = val
    ElMessage.success(`「${row.roleName}」已${val ? '启用' : '停用'}`)
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

onMounted(loadRoles)
</script>

<template>
  <div>
    <div class="page-header">
      <div>
        <h2>角色管理</h2>
        <p class="desc">角色可新增、编辑、删除、启停；权限按角色动态分配</p>
      </div>
      <el-button v-permission="'dict:manage'" type="primary" @click="openCreate">新增角色</el-button>
    </div>

    <el-table :data="roles" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="64" />
      <el-table-column prop="roleCode" label="角色编码" width="130" />
      <el-table-column prop="roleName" label="角色名称" width="130" />
      <el-table-column prop="roleDesc" label="描述" min-width="150" />
      <el-table-column label="已分配权限" min-width="210">
        <template #default="{ row }">
          <template v-if="permLabel(row).length">
            <el-tag
              v-for="n in permLabel(row).slice(0, 3)"
              :key="n"
              size="small"
              class="perm-tag"
            >
              {{ n }}
            </el-tag>
            <span v-if="permLabel(row).length > 3" class="more">
              +{{ permLabel(row).length - 3 }}
            </span>
          </template>
          <span v-else class="none">未分配</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="96">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status"
            :active-value="1"
            :inactive-value="0"
            :disabled="row.roleCode === 'ADMIN'"
            @change="(v) => changeStatus(row, v)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="168" />
      <el-table-column label="操作" width="238" fixed="right">
        <template #default="{ row }">
          <el-button
            v-permission="'dict:manage'"
            size="small"
            type="primary"
            plain
            :disabled="row.roleCode === 'ADMIN'"
            @click="openAssign(row)"
          >
            分配权限
          </el-button>
          <el-button v-permission="'dict:manage'" size="small" @click="openEdit(row)">
            编辑
          </el-button>
          <el-button
            v-permission="'dict:manage'"
            size="small"
            type="danger"
            plain
            :disabled="row.roleCode === 'ADMIN'"
            @click="removeRole(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分配权限 -->
    <el-dialog v-model="assignVisible" title="分配权限" width="520px" destroy-on-close>
      <p class="dlg-tip">
        角色：<b>{{ assignRow?.roleName }}</b> — 勾选该角色可访问的菜单和按钮
      </p>
      <el-tree
        ref="treeRef"
        :data="permTree"
        show-checkbox
        node-key="id"
        :default-checked-keys="checkedKeys"
        :props="{ label: 'permName', children: 'children' }"
        default-expand-all
      />
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAssign">确定</el-button>
      </template>
    </el-dialog>

    <!-- 新增 / 编辑 -->
    <el-dialog
      v-model="formVisible"
      :title="formMode === 'create' ? '新增角色' : '编辑角色'"
      width="600px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="86px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input
            v-model="form.roleCode"
            :disabled="formMode === 'edit'"
            placeholder="如 LIBRARIAN"
          />
          <p class="tip">
            大写字母开头，可含数字和下划线；创建后不可修改，权限按编码绑定
          </p>
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="如 图书管理员" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="form.roleDesc"
            type="textarea"
            :rows="2"
            placeholder="这个角色能做什么"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="停用"
          />
        </el-form-item>
        <el-form-item v-if="formMode === 'create'" label="分配权限">
          <div class="tree-box">
            <el-tree
              ref="formTreeRef"
              :data="permTree"
              show-checkbox
              node-key="id"
              :default-checked-keys="formPermKeys"
              :props="{ label: 'permName', children: 'children' }"
              default-expand-all
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}
.page-header h2 {
  margin: 0 0 6px;
  font-size: 20px;
  color: #333;
}
.desc {
  margin: 0;
  color: #888;
  font-size: 13px;
}
.perm-tag {
  margin-right: 4px;
  margin-bottom: 2px;
}
.more {
  color: #909399;
  font-size: 12px;
}
.none {
  color: #c0c4cc;
  font-size: 12px;
}
.dlg-tip {
  margin: 0 0 12px;
  color: #666;
  font-size: 13px;
}
.tip {
  margin: 4px 0 0;
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
}
.tree-box {
  width: 100%;
  max-height: 240px;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 8px 4px;
}
</style>
