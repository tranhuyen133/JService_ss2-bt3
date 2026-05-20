package re.bt3.controller;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
//HTTP Status Code giúp API giao tiếp rõ ràng với client về kết quả xử lý request.
// Nhờ đó client có thể biết request thành công hay thất bại để xử lý phù hợp. Ví dụ:
//
//200 OK: xử lý thành công
//201 Created: tạo dữ liệu thành công
//204 No Content: xóa thành công
//404 Not Found: không tìm thấy dữ liệu
//500 Internal Server Error: lỗi server
//
//Nếu API trả về null thay vì ResponseEntity với HttpStatus.NOT_FOUND, client sẽ khó phân biệt được:
//
//dữ liệu thật sự không tồn tại
//hay server bị lỗi
//hay response bị sai định dạng
//
//Điều này làm frontend/mobile xử lý lỗi không chính xác và dễ gây bug.
//
//Ngoài ra, để hỗ trợ Content Negotiation giữa JSON và XML, Spring Boot cần dependency Jackson Dataformat XML.
// Mặc định Spring chỉ hỗ trợ JSON thông qua Jackson. Khi thêm thư viện XML, Spring mới có thể tự động convert
// object Java sang XML khi client gửi header:
//
//Accept: application/xml
@RestController
@RequestMapping("/api/items")
public class ItemController {

    private List<Item> items = new ArrayList<>();
    private AtomicLong nextId = new AtomicLong(1);

    @JacksonXmlRootElement(localName = "item")
    static class Item {

        private Long id;
        private String name;
        private int quantity;

        public Item() {
        }

        public Item(Long id, String name, int quantity) {
            this.id = id;
            this.name = name;
            this.quantity = quantity;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }


    @GetMapping(value = "/{id}",
            produces = {
                    MediaType.APPLICATION_JSON_VALUE,
                    MediaType.APPLICATION_XML_VALUE
            })
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {

        return items.stream()
                .filter(i -> i.getId().equals(id))
                .findFirst()
                .map(i -> new ResponseEntity<>(i, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    @PostMapping(
            produces = {
                    MediaType.APPLICATION_JSON_VALUE,
                    MediaType.APPLICATION_XML_VALUE
            })
    public ResponseEntity<Item> createItem(@RequestBody Item item) {

        item.setId(nextId.getAndIncrement());

        items.add(item);

        return new ResponseEntity<>(item, HttpStatus.CREATED);
    }


    @PutMapping(value = "/{id}",
            produces = {
                    MediaType.APPLICATION_JSON_VALUE,
                    MediaType.APPLICATION_XML_VALUE
            })
    public ResponseEntity<Item> updateItem(
            @PathVariable Long id,
            @RequestBody Item item) {

        Optional<Item> existingItem = items.stream()
                .filter(i -> i.getId().equals(id))
                .findFirst();

        if (existingItem.isPresent()) {

            Item i = existingItem.get();

            i.setName(item.getName());
            i.setQuantity(item.getQuantity());

            return new ResponseEntity<>(i, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {

        Optional<Item> existingItem = items.stream()
                .filter(i -> i.getId().equals(id))
                .findFirst();

        if (existingItem.isPresent()) {

            items.remove(existingItem.get());

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
