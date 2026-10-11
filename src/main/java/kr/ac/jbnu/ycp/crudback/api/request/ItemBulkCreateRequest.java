package kr.ac.jbnu.ycp.crudback.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class ItemBulkCreateRequest {
    @NotEmpty(message = "생성할 아이템이 최소 1개 이상 필요합니다.")
    private List<@Valid ItemCreateRequest> items;

    // getter / setter
    public List<ItemCreateRequest> getItems() { return items; }
    public void setItems(List<ItemCreateRequest> items) { this.items = items; }
}
