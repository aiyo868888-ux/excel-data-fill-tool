<template>
  <div class="categories">
    <el-card>
      <!-- 工具栏 -->
      <div class="toolbar">
        <el-button type="primary" @click="showAddDialog">
          <el-icon><Plus /></el-icon>
          添加分类
        </el-button>
        <el-button @click="loadCategories">刷新</el-button>
      </div>

      <!-- 分类列表 -->
      <el-table :data="categories" style="margin-top: 20px;" v-loading="loading" row-key="_id">
        <el-table-column prop="name" label="分类名称" width="200" />
        <el-table-column prop="parentId" label="父级ID" width="200">
          <template #default="{ row }">
            {{ row.parentId || '顶级分类' }}
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="100" />
        <el-table-column prop="icon" label="图标路径" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="editCategory(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteCategory(row._id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : '添加分类'"
      width="500px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="分类名称">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="父级ID">
          <el-input v-model="form.parentId" placeholder="留空则为顶级分类" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="1" />
        </el-form-item>
        <el-form-item label="图标路径">
          <el-input v-model="form.icon" placeholder="/images/category-1.png" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCategory">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';

const loading = ref(false);
const categories = ref([]);
const dialogVisible = ref(false);
const isEdit = ref(false);
const form = ref({
  name: '',
  parentId: '',
  sortOrder: 1,
  icon: ''
});

const loadCategories = async () => {
  loading.value = true;
  try {
    const res = await axios.get('/api/categories');
    if (res.data.success) {
      categories.value = res.data.data;
    }
  } catch (error) {
    ElMessage.error('加载分类列表失败');
  } finally {
    loading.value = false;
  }
};

const showAddDialog = () => {
  isEdit.value = false;
  form.value = {
    name: '',
    parentId: '',
    sortOrder: 1,
    icon: ''
  };
  dialogVisible.value = true;
};

const editCategory = (category) => {
  isEdit.value = true;
  form.value = { ...category };
  dialogVisible.value = true;
};

const saveCategory = async () => {
  try {
    if (isEdit.value) {
      await axios.put(`/api/categories/${form.value._id}`, form.value);
      ElMessage.success('更新成功');
    } else {
      await axios.post('/api/categories', form.value);
      ElMessage.success('添加成功');
    }
    dialogVisible.value = false;
    loadCategories();
  } catch (error) {
    ElMessage.error('保存失败');
  }
};

const deleteCategory = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个分类吗？', '提示', {
      type: 'warning'
    });
    await axios.delete(`/api/categories/${id}`);
    ElMessage.success('删除成功');
    loadCategories();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

onMounted(() => {
  loadCategories();
});
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
}
</style>
