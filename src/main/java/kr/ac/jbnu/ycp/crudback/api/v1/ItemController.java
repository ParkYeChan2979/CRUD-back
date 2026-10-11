package kr.ac.jbnu.ycp.crudback.api.v1;

import jakarta.validation.Valid;
import kr.ac.jbnu.ycp.crudback.api.dto.ApiResponse;
import kr.ac.jbnu.ycp.crudback.api.dto.ItemDto;
import kr.ac.jbnu.ycp.crudback.api.exception.ApiException;
import kr.ac.jbnu.ycp.crudback.api.request.ItemBulkCreateRequest;
import kr.ac.jbnu.ycp.crudback.api.request.ItemCreateRequest;
import kr.ac.jbnu.ycp.crudback.api.request.ItemPriceUpdateRequest;
import kr.ac.jbnu.ycp.crudback.api.request.ItemUpdateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/v1/items")
public class ItemController {

    // 메모리용 임시 DB (id 순으로 정렬되도록 TreeMap 사용)
    private final Map<Long, ItemDto> store = new TreeMap<>();
    private long sequence = 1L;

    // 1) POST: 생성 → 201 Created / 400 / 409
    @PostMapping
    public synchronized ResponseEntity<ApiResponse<ItemDto>> createItem(
            @Valid @RequestBody ItemCreateRequest request
    ) {
        checkDuplicateName(request.getName(), null);
        ItemDto item = save(request);

        return ResponseEntity.created(URI.create("/api/v1/items/" + item.getId()))
                .body(ApiResponse.success(item, "아이템이 생성되었습니다."));
    }

    // 2) POST: 일괄 생성 → 201 Created / 400 / 409
    @PostMapping("/bulk")
    public synchronized ResponseEntity<ApiResponse<List<ItemDto>>> createItems(
            @Valid @RequestBody ItemBulkCreateRequest request
    ) {
        // 하나라도 중복이면 아무것도 저장하지 않는다
        Set<String> names = new HashSet<>();
        for (ItemCreateRequest itemRequest : request.getItems()) {
            if (!names.add(itemRequest.getName())) {
                throw new ApiException(HttpStatus.CONFLICT, "요청 안에 중복된 이름이 있습니다: " + itemRequest.getName());
            }
            checkDuplicateName(itemRequest.getName(), null);
        }

        List<ItemDto> items = request.getItems().stream().map(this::save).toList();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(items, items.size() + "개의 아이템이 생성되었습니다."));
    }

    // 3) GET: 전체 조회 (keyword 로 이름 검색 가능) → 200 OK
    @GetMapping
    public synchronized ResponseEntity<ApiResponse<List<ItemDto>>> getItems(
            @RequestParam(required = false) String keyword
    ) {
        List<ItemDto> items = store.values().stream()
                .filter(item -> keyword == null || item.getName().contains(keyword))
                .toList();

        return ResponseEntity.ok(ApiResponse.success(items));
    }

    // 4) GET: 단건 조회 (PathVariable 사용) → 200 OK / 404
    @GetMapping("/{id}")
    public synchronized ResponseEntity<ApiResponse<ItemDto>> getItem(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(findItem(id)));
    }

    // 5) PUT: 이름·가격 전체 수정 → 200 OK / 400 / 404 / 409
    @PutMapping("/{id}")
    public synchronized ResponseEntity<ApiResponse<ItemDto>> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemUpdateRequest request
    ) {
        ItemDto item = findItem(id);
        checkDuplicateName(request.getName(), id);

        item.setName(request.getName());
        item.setPrice(request.getPrice());

        return ResponseEntity.ok(ApiResponse.success(item, "아이템이 수정되었습니다."));
    }

    // 6) PUT: 가격만 수정 → 200 OK / 400 / 404
    @PutMapping("/{id}/price")
    public synchronized ResponseEntity<ApiResponse<ItemDto>> updatePrice(
            @PathVariable Long id,
            @Valid @RequestBody ItemPriceUpdateRequest request
    ) {
        ItemDto item = findItem(id);
        item.setPrice(request.getPrice());

        return ResponseEntity.ok(ApiResponse.success(item, "가격이 수정되었습니다."));
    }

    // 7) DELETE: 단건 삭제 → 200 OK / 404
    @DeleteMapping("/{id}")
    public synchronized ResponseEntity<ApiResponse<ItemDto>> deleteItem(@PathVariable Long id) {
        ItemDto removed = findItem(id);
        store.remove(id);

        return ResponseEntity.ok(ApiResponse.success(removed, "아이템이 삭제되었습니다."));
    }

    // 8) DELETE: 전체 삭제 → 200 OK
    @DeleteMapping
    public synchronized ResponseEntity<ApiResponse<Map<String, Integer>>> deleteAllItems() {
        int count = store.size();
        store.clear();

        return ResponseEntity.ok(ApiResponse.success(Map.of("deletedCount", count), "전체 아이템이 삭제되었습니다."));
    }

    // 없는 id 면 404 Not Found
    private ItemDto findItem(Long id) {
        ItemDto item = store.get(id);
        if (item == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "id=" + id + " 아이템을 찾을 수 없습니다.");
        }
        return item;
    }

    // 같은 이름의 아이템이 이미 있으면 409 Conflict (excludeId 는 자기 자신 제외용)
    private void checkDuplicateName(String name, Long excludeId) {
        boolean exists = store.values().stream()
                .anyMatch(item -> item.getName().equals(name) && !item.getId().equals(excludeId));
        if (exists) {
            throw new ApiException(HttpStatus.CONFLICT, "이미 존재하는 이름입니다: " + name);
        }
    }

    private ItemDto save(ItemCreateRequest request) {
        ItemDto item = new ItemDto();
        item.setId(sequence++);
        item.setName(request.getName());
        item.setPrice(request.getPrice());

        store.put(item.getId(), item);
        return item;
    }
}
