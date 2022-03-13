package Monopolist;

import generated.PallierType;
import generated.ProductType;
import generated.World;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("monopolist/generic")
@CrossOrigin
public class Webservice {
    Services services;

    public Webservice() {
        services = new Services();
    }

    @GetMapping(value = "world", produces = {"application/xml", "application/json"})
    public ResponseEntity<World> getWorld(@RequestHeader(value = "X-User", required = false) String username) {
        World world = services.getWorld(username);
        return ResponseEntity.ok(world);
    }

    @PutMapping(value = "product", consumes = {"application/xml", "application/json"})
    public ProductType updateProduct(
            @RequestHeader(value = "X-User", required = false) String username,
            @RequestBody ProductType productType) {
        if (services.updateProduct(username, productType)) {
            return productType;
        } else {
            return null;
        }
    }

    @PutMapping(value = "manager", consumes = {"application/xml", "application/json"})
    public PallierType updateManager(
            @RequestHeader(value = "X-User", required = false) String username,
            @RequestBody PallierType pallierType) {
        if (services.updateManager(username, pallierType)) {
            return pallierType;
        } else {
            return null;
        }
    }
/*
    @PutMapping(name="manager", produces = {"application/xml", "application/json"} )
    public PallierType upgrade(
            @RequestHeader(value = "X-User", required = false) String username,
            @RequestBody PallierType pallierType) {
        if (services.updateManager(username, pallierType)) {
            return pallierType;
        }
        else {
            return null;
        }
    }

 */
}