<template>
  <div class="app-container">
    <!-- 顶部搜索 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="销售状态" prop="status">

        <el-select   style="width: 200px" v-model="queryParams.status" placeholder="请选择销售状态" clearable>
          <el-option
              v-for="dict in sale_status"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
          />
        </el-select>

      </el-form-item>
      <el-form-item label="创建人" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="请输入创建人"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 顶部按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['erp:sale:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['erp:sale:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['erp:sale:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['erp:sale:export']"
        >导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="primary"
            icon="Upload"
            size="mini"
            @click="handleImport"
        >导入</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 表格 -->
    <el-table @row-click="clickRow" ref="table" highlight-current-row
              border v-loading="loading" :data="saleList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" align="center" type="index" :index="indexMethod" />

      <el-table-column label="订单总金额" align="center" prop="totalAmount" />
      <el-table-column label="销售状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :options="sale_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="创建人" align="center" prop="userName" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['erp:sale:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['erp:sale:remove']">删除</el-button>
          <el-button link v-if="scope.row.status === '待发货'" type="primary" icon="Van" @click="handleUp(scope.row)" >
            发货</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 导入对话框 -->
    <vxe-modal :title="upload.title" v-model="upload.open" width="400px" showFooter show-zoom resize>
      <el-upload
          ref="uploadRef"
          :limit="1"
          accept=".xlsx, .xls"
          :headers="upload.headers"
          :action="upload.url"
          :data="{ updateSupport: upload.updateSupport }"
          :disabled="upload.isUploading"
          :on-progress="handleFileUploadProgress"
          :on-success="handleFileSuccess"
          :auto-upload="false"
          drag
      >
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <div class="el-upload__tip text-center" slot="tip">
          <span>仅允许导入xls、xlsx格式文件。</span>
          <el-link type="primary" :underline="false" style="font-size:12px;vertical-align: baseline;" @click="importTemplate">下载模板</el-link>
        </div>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFileForm">确 定</el-button>
        <el-button @click="upload.open = false">取 消</el-button>
      </div>
    </vxe-modal>

    <!-- 添加或修改销售对话框 -->
    <vxe-modal :title="title" v-model="open" width="60%" show-maximize showFooter resize>
      <el-form ref="saleRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="订单总金额" prop="totalAmount">
          <el-input v-model="form.totalAmount" placeholder="请输入订单总金额" />
        </el-form-item>


        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注" />
        </el-form-item>

        <el-divider content-position="center">销售明细信息</el-divider>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" icon="Plus" @click="handleAddSaleItems">添加</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" icon="Delete" @click="handleDeleteSaleItems">删除</el-button>
          </el-col>
        </el-row>
        <el-table :data="saleItemsList" :row-class-name="rowSaleItemsIndex" @selection-change="handleSaleItemsSelectionChange" ref="saleItems">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column label="序号" align="center" prop="index" width="50"/>
          <el-table-column label="商品名称" prop="goodsName" align="center"/>
          <el-table-column label="销售数量" prop="total" align="center">
            <template #default="scope">
              <el-input v-model="scope.row.total" placeholder="请输入销售数量" />
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </vxe-modal>


    <!--商品多选组件-->
    <SelectGoods ref="selectGoods" @ok="handleOk"/>
  </div>
</template>

<script setup name="Sale">
import {listSale, getSale, delSale, addSale, updateSale, handleUps} from "@/api/erp/sale"
import {getToken} from "@/utils/auth.js";
import SelectGoods from "@/views/erp/goods/SelectGoods.vue";
import {ElMessage, ElMessageBox} from "element-plus";
const baseURL = import.meta.env.VITE_APP_BASE_API

const { proxy } = getCurrentInstance()
const { sale_status } = proxy.useDict('sale_status')

const saleList = ref([])
const saleItemsList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const checkedSaleItems = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const selectedRow = ref(null)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    status: null,
    userId: null,
    userName: null,
  },
  rules: {
    totalAmount: [
      { required: true, message: "订单总金额不能为空", trigger: "blur" }
    ],


  },
  // 导入参数
  upload: {
    // 是否显示弹出层（导入）
    open: false,
    // 弹出层标题（导入）
    title: "",
    // 是否禁用上传
    isUploading: false,
    // 设置上传的请求头部
    headers: { Authorization: "Bearer " + getToken() },
    // 上传的地址
    url: baseURL + "/erp/sale/importData"
  }
})

const { queryParams, form, rules, upload } = toRefs(data)




//发货
const handleUp = (row) => {

  ElMessageBox.confirm(
      '确定发货吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(() => {
        getSale(row.saleId).then(res => {
          const saleItemsList = res.data.saleItemsList


          const data = {
            saleId: row.saleId,
            status: '已发货',
            saleItemsList: saleItemsList
          }

            handleUps(data).then(res => {
            getList()
            ElMessage({
              type: 'success',
              message: '发货成功',
            })
          })
        })
      })
      .catch(() => {
        ElMessage({
          type: 'info',
          message: '发货失败',
        })
      })


}



//选择商品组件点击确定后的数据回调
const handleOk = (selectRows) => {
  //将新增商品添加到列表中
  selectRows.forEach(item => {
    saleItemsList.value.push(item)
  })
}


//实例化商品多选组件
const selectGoods = ref(null)
/** 销售明细添加按钮操作 */
const handleAddSaleItems = () => {
  selectGoods.value.handleOpen()
}


//点击行 获取行
const clickRow = (row) => {
  selectedRow.value = row; // 更新选中的行
  const table = proxy.$refs.table;
  // 清除所有已选中的行
  table.clearSelection();
  // 选中当前点击的行
  table.toggleRowSelection(row, true);
}

/** 自定义序号 */
const indexMethod = (index) => {
  let pageNum = queryParams.value.pageNum - 1;
  if((pageNum!==-1 && pageNum!==0)){
    return (index + 1) + (pageNum  * queryParams.value.pageSize);
  }else{
    return (index + 1)
  }
}

/** 查询销售列表 */
const getList = () => {
  loading.value = true
  listSale(queryParams.value).then(response => {
    saleList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

// 取消按钮
const cancel = () => {
  open.value = false
  reset()
}

// 表单重置
const reset = () => {
  form.value = {
    saleId: null,
    totalAmount: null,
    status: null,
    remark: null,
    userId: null,
    createTime: null
  }
  saleItemsList.value = []
  proxy.resetForm("saleRef")
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  proxy.resetForm("queryRef")
  handleQuery()
}

// 多选框选中数据
const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.saleId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
const handleAdd = () => {
  reset()
  open.value = true
  title.value = "添加销售"
}

/** 修改按钮操作 */
const handleUpdate = (row) => {
  reset()
  const _saleId = row.saleId || ids.value
  getSale(_saleId).then(response => {
    form.value = response.data
    saleItemsList.value = response.data.saleItemsList
    open.value = true
    title.value = "修改销售"
  })
}

/** 提交按钮 */
const submitForm = () => {
  proxy.$refs["saleRef"].validate(valid => {
    if (valid) {
      form.value.saleItemsList = saleItemsList.value
      if (form.value.saleId != null) {
        updateSale(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addSale(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
const handleDelete = (row) => {
  const _saleIds = row.saleId || ids.value
  proxy.$modal.confirm('是否确认删除该项数据？').then(function() {
    return delSale(_saleIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 销售明细序号 */
const rowSaleItemsIndex = ({ row, rowIndex }) => {
  row.index = rowIndex + 1
}



/** 销售明细删除按钮操作 */
const handleDeleteSaleItems = () => {
  if (checkedSaleItems.value.length == 0) {
    proxy.$modal.msgError("请先选择要删除的销售明细数据")
  } else {
    const saleItemss = saleItemsList.value
    const checkedSaleItemss = checkedSaleItems.value
    saleItemsList.value = saleItemss.filter(function(item) {
      return checkedSaleItemss.indexOf(item.index) == -1
    })
  }
}

/** 复选框选中数据 */
const handleSaleItemsSelectionChange = (selection) => {
  checkedSaleItems.value = selection.map(item => item.index)
}

/** 导出按钮操作 */
const handleExport = () => {
  proxy.download('erp/sale/export', {
    ...queryParams.value
  }, `sale_${new Date().getTime()}.xlsx`)
}

/** 下载模板操作 */
const importTemplate = () => {
  proxy.download('erp/sale/importTemplate', {
  }, `template_${new Date().getTime()}.xlsx`)
}

/** 导入按钮操作 */
const handleImport = () => {
  upload.value.title = "销售导入";
  upload.value.open = true;
}

// 文件上传中处理
const handleFileUploadProgress = (event, file, fileList) => {
  upload.value.isUploading = true;
}

// 文件上传成功处理
const handleFileSuccess = (response, file, fileList) => {
  upload.value.open = false;
  upload.value.isUploading = false;
  proxy.$refs.uploadRef.clearFiles();
  proxy.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.msg + "</div>", "导入结果", { dangerouslyUseHTMLString: true });
  getList();
}

// 提交上传文件
const submitFileForm = () => {
  proxy.$refs.uploadRef.submit();
}

getList()
</script>
