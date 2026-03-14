<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="分类名称" prop="classifyName">
        <el-input
            v-model="queryParams.classifyName"
            placeholder="请输入分类名称"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
            type="primary"
            plain
            icon="Plus"
            @click="handleAdd"
            v-hasPermi="['erp:sort:add']"
        >新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="info"
            plain
            icon="Sort"
            @click="toggleExpandAll"
        >展开/折叠
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table
        v-if="refreshTable"
        v-loading="loading"
        :data="sortList"
        row-key="sortId"
        :default-expand-all="isExpandAll"
        :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
    >
      <el-table-column label="分类名称" prop="classifyName"/>
      <el-table-column label="显示顺序" align="center" prop="orderNum"/>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['erp:sort:edit']">修改
          </el-button>
          <el-button link type="primary" icon="Plus" @click="handleAdd(scope.row)"
                     v-hasPermi="['erp:sort:add']">新增
          </el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['erp:sort:remove']">删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加或修改商品分类对话框 -->
    <vxe-modal :title="title" v-model="open" width="500px" show-maximize showFooter resize>
      <el-form ref="sortRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="上级分类ID" prop="parentId">
          <el-tree-select
              v-model="form.parentId"
              :data="sortOptions"
              :props="{ value: 'sortId', label: 'classifyName', children: 'children' }"
              value-key="sortId"
              placeholder="请选择上级分类ID"
              check-strictly
          />
        </el-form-item>
        <el-form-item label="分类名称" prop="classifyName">
          <el-input v-model="form.classifyName" placeholder="请输入分类名称"/>
        </el-form-item>
        <el-form-item label="显示顺序" prop="orderNum">
          <el-input v-model="form.orderNum" placeholder="请输入显示顺序"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </vxe-modal>
  </div>
</template>

<script setup name="Sort">
import {listSort, getSort, delSort, addSort, updateSort} from "@/api/erp/sort"

const {proxy} = getCurrentInstance()

const sortList = ref([])
const sortOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const title = ref("")
const isExpandAll = ref(true)
const refreshTable = ref(true)

const data = reactive({
  form: {},
  queryParams: {
    parentId: null,
    classifyName: null,
  },
  rules: {
    classifyName: [
      {required: true, message: "分类名称不能为空", trigger: "blur"}
    ],
    orderNum: [
      {required: true, message: "显示顺序不能为空", trigger: "blur"}
    ]
  }
})

const {queryParams, form, rules} = toRefs(data)

/** 查询商品分类列表 */
function getList() {
  loading.value = true
  listSort(queryParams.value).then(response => {
    sortList.value = proxy.handleTree(response.data, "sortId", "parentId")
    loading.value = false
  })
}

/** 查询商品分类下拉树结构 */
function getTreeselect() {
  listSort().then(response => {
    sortOptions.value = []
    const data = {sortId: 0, classifyName: '顶级节点', children: []}
    data.children = proxy.handleTree(response.data, "sortId", "parentId")
    sortOptions.value.push(data)
  })
}

// 取消按钮
function cancel() {
  open.value = false
  reset()
}

// 表单重置
function reset() {
  form.value = {
    sortId: null,
    parentId: null,
    classifyName: null,
    orderNum: null
  }
  proxy.resetForm("sortRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 新增按钮操作 */
function handleAdd(row) {
  reset()
  getTreeselect()
  if (row != null && row.sortId) {
    form.value.parentId = row.sortId
  } else {
    form.value.parentId = 0
  }
  open.value = true
  title.value = "添加商品分类"
}

/** 展开/折叠操作 */
function toggleExpandAll() {
  refreshTable.value = false
  isExpandAll.value = !isExpandAll.value
  nextTick(() => {
    refreshTable.value = true
  })
}

/** 修改按钮操作 */
async function handleUpdate(row) {
  reset()
  await getTreeselect()
  if (row != null) {
    form.value.parentId = row.parentId
  }
  getSort(row.sortId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改商品分类"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["sortRef"].validate(valid => {
    if (valid) {
      if (form.value.sortId != null) {
        updateSort(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addSort(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除商品分类编号为"' + row.sortId + '"的数据项？').then(function () {
    return delSort(row.sortId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {
  })
}

getList()
</script>

