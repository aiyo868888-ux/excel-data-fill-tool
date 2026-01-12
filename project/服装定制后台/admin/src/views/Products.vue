<template>
  <div class="products">
    <el-card>
      <!-- 工具栏 -->
      <div class="toolbar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品名称"
          style="width: 200px; margin-right: 10px;"
          @keyup.enter="loadProducts"
        />
        <el-button type="primary" @click="showAddDialog">
          <el-icon><Plus /></el-icon>
          添加商品
        </el-button>
        <el-button @click="loadProducts">刷新</el-button>
      </div>

      <!-- 商品列表 -->
      <el-table :data="products" style="margin-top: 20px;" v-loading="loading">
        <el-table-column prop="name" label="商品名称" width="200" />
        <el-table-column prop="categoryId" label="分类ID" width="150" />
        <el-table-column prop="price" label="价格" width="100" />
        <el-table-column prop="sales" label="销量" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="editProduct(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteProduct(row._id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        @current-change="loadProducts"
        layout="total, prev, pager, next"
        style="margin-top: 20px; text-align: right;"
      />
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑商品' : '添加商品'"
      width="600px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="商品名称">
          <el-input v-model="form.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="分类ID">
          <el-input v-model="form.categoryId" placeholder="请输入分类ID" />
        </el-form-item>
        <el-form-item label="类型">
          <el-input v-model="form.type" placeholder="如：三合一、圆领等" />
        </el-form-item>
        <el-form-item label="材质">
          <el-input v-model="form.material" placeholder="如：100%聚酯纤维" />
        </el-form-item>
        <el-form-item label="款式">
          <el-input v-model="form.style" placeholder="如：通款" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.price" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入商品描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProduct">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';

const loading = ref(false);
const products = ref([]);
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const searchKeyword = ref('');
const dialogVisible = ref(false);
const isEdit = ref(false);
const form = ref({
  name: '',
  categoryId: '',
  type: '',
  material: '',
  style: '',
  price: 0,
  status: 1,
  description: ''
});

const loadProducts = async () => {
  loading.value = true;
  try {
    const res = await axios.get('/api/products', {
      params: {
        page: page.value,
        limit: pageSize.value,
        keyword: searchKeyword.value
      }
    });

    if (res.data.success) {
      products.value = res.data.data.list;
      total.value = res.data.data.total;
    }
  } catch (error) {
    ElMessage.error('加载商品列表失败');
  } finally {
    loading.value = false;
  }
};

const showAddDialog = () => {
  isEdit.value = false;
  form.value = {
    name: '',
    categoryId: '',
    type: '',
    material: '',
    style: '',
    price: 0,
    status: 1,
    description: ''
  };
  dialogVisible.value = true;
};

const editProduct = (product) => {
  isEdit.value = true;
  form.value = { ...product };
  dialogVisible.value = true;
};

const saveProduct = async () => {
  try {
    if (isEdit.value) {
      await axios.put(`/api/products/${form.value._id}`, form.value);
      ElMessage.success('更新成功');
    } else {
      await axios.post('/api/products', form.value);
      ElMessage.success('添加成功');
    }
    dialogVisible.value = false;
    loadProducts();
  } catch (error) {
    ElMessage.error('保存失败');
  }
};

const deleteProduct = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个商品吗？', '提示', {
      type: 'warning'
    });
    await axios.delete(`/api/products/${id}`);
    ElMessage.success('删除成功');
    loadProducts();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

onMounted(() => {
  loadProducts();
});
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
}
</style>
