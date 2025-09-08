package com.generatescript;

public class Item {
    private String productCode;
    private String processDefinitionName;

    public Item(String productCode, String processDefinitionName) {
        this.productCode = productCode;
        this.processDefinitionName = processDefinitionName;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProcessDefinitionName() {
        return processDefinitionName;
    }

    public void setProcessDefinitionName(String processDefinitionName) {
        this.processDefinitionName = processDefinitionName;
    }

    @Override
    public String toString() {
        return String.format("{'code':'%s', 'process':'%s'}\n", productCode, processDefinitionName);
    }
}
