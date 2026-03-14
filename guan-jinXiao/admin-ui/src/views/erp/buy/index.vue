<template>
  <div class="app-container">
    <!-- 顶部搜索 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="采购状态" prop="status">
        <el-select style="width: 200px" v-model="queryParams.status" placeholder="请选择采购状态" clearable>
          <el-option
              v-for="dict in buy_status"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="供应商" prop="supplierId">
        <!--    <el-input
              v-model="queryParams.supplierId"
              placeholder="请输入供应商"
              clearable
              @keyup.enter="handleQuery"
            />-->
        <el-select
            v-model="form.supplierId"
            placeholder="请选择供应商"
            style="width: 200px;"
        >
          <el-option
              v-for="item in AllSupplierList"
              :key="item.supplierId"
              :label="item.supplierName"
              :value="item.supplierId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="创建人" prop="userName">
        <el-input
            v-model="queryParams.userName"
            placeholder="请输入创建人用户名"
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
            v-hasPermi="['erp:buy:add']"
        >新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="success"
            plain
            icon="Edit"
            :disabled="single"
            @click="handleUpdate"
            v-hasPermi="['erp:buy:edit']"
        >修改
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="danger"
            plain
            icon="Delete"
            :disabled="multiple"
            @click="handleDelete"
            v-hasPermi="['erp:buy:remove']"
        >删除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="warning"
            plain
            icon="Download"
            @click="handleExport"
            v-hasPermi="['erp:buy:export']"
        >导出
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="primary"
            icon="Upload"
            size="mini"
            @click="handleImport"
        >导入
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 表格 -->
    <el-table @row-click="clickRow" ref="table" highlight-current-row
              border v-loading="loading" :data="buyList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="序号" align="center" type="index" :index="indexMethod"/>
      <el-table-column label="采购编号" align="center" prop="buyId">

        <template #default="scope">
          <el-link  type="primary" @click="getBuyDetail(scope.row.buyId)">{{scope.row.buyId}}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="订单总金额" align="center" prop="totalAmount"/>
      <el-table-column label="采购状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :options="buy_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="供应商" align="center" prop="supplierName"/>
      <el-table-column label="备注" align="center" prop="remark"/>
      <el-table-column label="创建人" align="center" prop="userName"/>
      <el-table-column label="创建时间" align="center" prop="createTime"/>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="250">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['erp:buy:edit']">
            修改
          </el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['erp:buy:remove']">
            删除
          </el-button>
          <el-button   v-if="scope.row.status==='待审核'" link type="primary" icon="Select" @click="handleYes(scope.row)">
            审批通过
          </el-button>
          <el-button  v-if="scope.row.status==='待审核'" link type="primary" icon="Close" @click="handleNo(scope.row)">
            审批不通过
          </el-button>
          <el-button  v-if="scope.row.status==='待审核'" link type="primary" icon="ShoppingTrolley" @click="handleReach(scope.row)">
            已到货
          </el-button>
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
          <el-link type="primary" :underline="false" style="font-size:12px;vertical-align: baseline;"
                   @click="importTemplate">下载模板
          </el-link>
        </div>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFileForm">确 定</el-button>
        <el-button @click="upload.open = false">取 消</el-button>

      </div>
    </vxe-modal>

    <!-- 添加或修改采购对话框 -->
    <vxe-modal :title="title" v-model="open" width="45%" show-maximize showFooter resize heght="70vh">
      <el-form ref="buyRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="订单总金额(元)" prop="totalAmount">
          <el-input disabled v-model="form.totalAmount" placeholder="自动计算"/>
        </el-form-item>


        <el-form-item label="供应商" prop="supplierId">
          <el-select
              v-model="form.supplierId"
              placeholder="请选择供应商"
          >
            <el-option
                v-for="item in AllSupplierList"
                :key="item.supplierId"
                :label="item.supplierName"
                :value="item.supplierId"
            />
          </el-select>
          <!--    <el-input v-model="form.supplierId" placeholder="请输入供应商" />-->
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注"/>
        </el-form-item>

        <el-divider content-position="center">采购明细信息</el-divider>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" icon="Plus" @click="handleAddBuyItems">添加</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" icon="Delete" @click="handleDeleteBuyItems">删除</el-button>
          </el-col>
        </el-row>
        <el-table :data="buyItemsList" :row-class-name="rowBuyItemsIndex"
                  @selection-change="handleBuyItemsSelectionChange" ref="buyItems">
          <el-table-column type="selection" width="50" align="center"/>
          <el-table-column label="序号" align="center" prop="index" width="50"/>

          <el-table-column label="商品名称" align="center" prop="goodsName"/>
          <el-table-column label="采购数量" align="center" prop="total">
            <template #default="scope">
              <el-input v-model="scope.row.total" placeholder="请输入采购数量"/>
            </template>
          </el-table-column>


          <el-table-column label="采购单价" align="center" prop="unitPrice">
            <template #default="scope">
              <el-input v-model="scope.row.unitPrice" placeholder="请输入采购单价"/>
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

    <!--采购详情组件-->
    <BuyDetail ref="buyDetail"/>
  </div>
</template>

<script setup name="Buy">
import {listBuy, getBuy, delBuy, addBuy, updateBuy, reach} from "@/api/erp/buy"
import {getToken} from "@/utils/auth.js";
import {selectAllSupplierList} from "@/api/erp/supplier.js";

import SelectGoods from "@/views/erp/goods/SelectGoods.vue";
import {ElMessage, ElMessageBox} from "element-plus";
import BuyDetail from "@/views/erp/buy/BuyDetail.vue";


const baseURL = import.meta.env.VITE_APP_BASE_API

const {proxy} = getCurrentInstance()
const {buy_status} = proxy.useDict('buy_status')

const buyList = ref([])
const buyItemsList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const checkedBuyItems = ref([])
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
    supplierId: null,
    userId: null,
    userName: null,
  },
  rules: {


    supplierId: [
      {required: true, message: "供应商不能为空", trigger: "blur"}
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
    headers: {Authorization: "Bearer " + getToken()},
    // 上传的地址
    url: baseURL + "/erp/buy/importData"
  }
})

const {queryParams, form, rules, upload} = toRefs(data)

//实例化采购详情组件
const buyDetail = ref(null)
//打开采购详情组件
const getBuyDetail = (buyId) => {
  buyDetail.value.handleOpen(buyId)
}

//审批通过
const handleYes = (row) => {

  ElMessageBox.confirm(
      '确认审批通过',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(() => {
        getBuy(row.buyId).then(res => {
          const buyItemsList = res.data.buyItemsList


          const data = {
            buyId: row.buyId,
            status: '已通过',
            buyItemsList: buyItemsList
          }

          updateBuy(data).then(res => {
            getList()
            ElMessage({
              type: 'success',
              message: '审批通过',
            })
          })
        })
      })
      .catch(() => {
        ElMessage({
          type: 'info',
          message: '取消审批',
        })
      })


}

//审批不通过
const handleNo = (row) => {
  ElMessageBox.confirm(
      '确认审批不通过',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(() => {
        getBuy(row.buyId).then(res => {
          const buyItemsList = res.data.buyItemsList


          const data = {
            buyId: row.buyId,
            status: '不通过',
            buyItemsList: buyItemsList
          }

          updateBuy(data).then(res => {
            getList()
            ElMessage({
              type: 'success',
              message: '审批不通过',
            })
          })
        })
      })
      .catch(() => {
        ElMessage({
          type: 'info',
          message: '取消审批',
        })
      })


}

//已到货
const handleReach = (row) => {
  ElMessageBox.confirm(
      '确认已到货吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(() => {
        getBuy(row.buyId).then(res => {
          const buyItemsList = res.data.buyItemsList


          const data = {
            buyId: row.buyId,
            status: '已收货',
            buyItemsList: buyItemsList
          }

          reach(data).then(res => {
            getList()
            ElMessage({
              type: 'success',
              message: '到货成功',
            })
          })
        })
      })
      .catch(() => {
        ElMessage({
          type: 'info',
          message: '取消审批',
        })
      })

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
  if ((pageNum !== -1 && pageNum !== 0)) {
    return (index + 1) + (pageNum * queryParams.value.pageSize);
  } else {
    return (index + 1)
  }
}

/** 查询采购列表 */
const getList = () => {
  loading.value = true
  listBuy(queryParams.value).then(response => {
    buyList.value = response.rows
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
    buyId: null,
    totalAmount: null,
    status: null,
    supplierId: null,
    remark: null,
    userId: null,
    createTime: null
  }
  buyItemsList.value = []
  proxy.resetForm("buyRef")
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
  ids.value = selection.map(item => item.buyId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
const handleAdd = () => {
  reset()
  open.value = true
  title.value = "添加采购"
}

/** 修改按钮操作 */
const handleUpdate = (row) => {
  reset()
  const _buyId = row.buyId || ids.value
  getBuy(_buyId).then(response => {
    form.value = response.data
    buyItemsList.value = response.data.buyItemsList
    open.value = true
    title.value = "修改采购"
  })
}

/** 提交按钮 */
const submitForm = () => {
  proxy.$refs["buyRef"].validate(valid => {
    if (valid) {
      form.value.buyItemsList = buyItemsList.value
      if (form.value.buyId != null) {
        updateBuy(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addBuy(form.value).then(response => {
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
  const _buyIds = row.buyId || ids.value
  proxy.$modal.confirm('是否确认删除该项数据？').then(function () {
    return delBuy(_buyIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {
  })
}

/** 采购明细序号 */
const rowBuyItemsIndex = ({row, rowIndex}) => {
  row.index = rowIndex + 1
}


//实例化商品多选组件
const selectGoods = ref(null)


/** 采购明细添加按钮操作 */
const handleAddBuyItems = () => {

  /*  let obj = {}
    obj.goodsId = ""
    obj.total = ""
    obj.unitPrice = ""
    buyItemsList.value.push(obj)*/
  selectGoods.value.handleOpen(buyItemsList.value)
}

/** 采购明细删除按钮操作 */
const handleDeleteBuyItems = () => {
  if (checkedBuyItems.value.length == 0) {
    proxy.$modal.msgError("请先选择要删除的采购明细数据")
  } else {
    const buyItemss = buyItemsList.value
    const checkedBuyItemss = checkedBuyItems.value
    buyItemsList.value = buyItemss.filter(function (item) {
      return checkedBuyItemss.indexOf(item.index) == -1
    })
  }
}

/** 复选框选中数据 */
const handleBuyItemsSelectionChange = (selection) => {
  checkedBuyItems.value = selection.map(item => item.index)
}

/** 导出按钮操作 */
const handleExport = () => {
  proxy.download('erp/buy/export', {
    ...queryParams.value
  }, `buy_${new Date().getTime()}.xlsx`)
}

/** 下载模板操作 */
const importTemplate = () => {
  proxy.download('erp/buy/importTemplate', {}, `template_${new Date().getTime()}.xlsx`)
}

/** 导入按钮操作 */
const handleImport = () => {
  upload.value.title = "采购导入";
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
  proxy.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.msg + "</div>", "导入结果", {dangerouslyUseHTMLString: true});
  getList();
}

// 提交上传文件
const submitFileForm = () => {
  proxy.$refs.uploadRef.submit();
}

//供应商列表数据
const AllSupplierList = ref([])
//不分页查询供应商
const getAllSupplierList = () => {
  selectAllSupplierList().then(res => {
    AllSupplierList.value = res.data
  })
}


/** 处理选择商品确定回调 */
/**
 * 处理选择商品确定回调 找到问题了！采购页面（ buy/index.vue ）中引用了 <SelectGoods @ok="handleOk" /> ，但没有定义 handleOk 方法来接收选择商品后的回调。

 问题分析
 SelectGoods 组件 emit 事件：在第 153 行定义了 emit('ok', selectedRows.value)
 父组件监听事件：在第 246 行监听了 @ok="handleOk"
 缺少处理方法：父组件中没有定义 handleOk 方法
 解决方案
 需要在 buy/index.vue 中添加 handleOk 方法，用于将选中的商品添加到采购明细列表中。

 请在 buy/index.vue 的 <script setup> 部分添加以下代码（建议添加在 handleAddBuyItems 方法后面）：*/
const handleOk = (selectedGoods) => {


  // selectedGoods 是选中的商品数组
  if (selectedGoods && selectedGoods.length > 0) {
    // 遍历选中的商品，添加到采购明细列表
    selectedGoods.forEach(goods => {
      // 创建采购明细对象
      const buyItem = {
        index: Date.now() + Math.random(), // 生成唯一索引
        goodsId: goods.goodsId,
        goodsName: goods.goodsName,
        unit: goods.unit,
        specs: goods.specs,
        total: null, // 采购数量（需要手动输入）
        unitPrice: null // 采购单价（需要手动输入）
      }
      // 添加到采购明细列表
      buyItemsList.value.push(buyItem)
    })
  }
}


//组件挂载时调用
getList()
getAllSupplierList()

</script>
