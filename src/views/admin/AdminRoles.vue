<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '../../mock/admin.js'

const roles = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const currentRole = ref(null)
const permTree = ref([])
const checkedKeys = ref([])
const treeRef = ref(null)

async function loadRoles() {
  loading.value = true
  try {
    roles.value = await adminApi.getRoles()
  } finally {
    loading.value = false
  }
}

async function openAssign(row) {
  currentRole.value = row
  permTree.value = await adminApi.getPermissionTree()
  const permIds = await adminApi.getRolePerms(row.id)
  checkedKeys.value = permIds
  dialogVisible.value = true
}

async function confirmAssign() {
  // 获取全选 + 半选（半选即父节点被部分选中，不存半选，只存全叶子和全选父节点）
  const leafChecked = treeRef.value.getCheckedKeys()
  const halfChecked = treeRef.value.getHalfCheckedKeys()
  const allKeys = [...leafChecked, ...halfChecked]
  await adminApi.assignRolePerms(currentRole.value.id, allKeys)
  ElMessage.success(`已更新角色「${currentRole.value.role_name}」的权限`)
  dialogVisible.value = false
}

onMounted(loadRoles)
</script>

<template>
  <div>
    <div class="page-header">
      <h2>角色管理</h2>
      <p class="desc">查看系统角色，点击"分配权限"为角色勾选可访问的菜单和按钮</p>
    </div>

    <el-table :data="roles" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="role_code" label="角色编码" width="140" />
      <el-table-column prop="role_name" label="角色名称" width="140" />
      <el-table-column prop="role_desc" label="描述" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="create_time" label="创建时间" width="180" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openAssign(row)">分配权限</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分配权限弹窗 -->
    <el-dialog v-model="dialogVisible" title="分配权限" width="520px">
      <p style="margin:0 0 12px;color:#666;font-size:13px">
        角色：<b>{{ currentRole?.role_name }}</b> — 勾选该角色可访问的菜单和按钮
      </p>
      <el-tree
        ref="treeRef"
        :data="permTree"
        show-checkbox
        node-key="id"
        :default-checked-keys="checkedKeys"
        :props="{ label: 'perm_name', children: 'children' }"
        default-expand-all
      />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAssign">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-header h2 { margin: 0 0 6px; font-size: 20px; color: #333; }
.desc { margin: 0 0 18px; color: #888; font-size: 13px; }
</style>
