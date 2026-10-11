package kr.ac.jbnu.ycp.crudback.api.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ItemPriceUpdateRequest {
    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private Integer price;

    // getter / setter
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
}
