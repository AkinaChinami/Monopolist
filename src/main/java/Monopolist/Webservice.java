package Monopolist;

import generated.PallierType;
import generated.ProductType;
import generated.World;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.invoke.DelegatingMethodHandle$Holder;

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

    @PutMapping(value = "upgrade", produces = {"application/xml", "application/json"} )
    public PallierType upgrade(
            @RequestHeader(value = "X-User", required = false) String username,
            @RequestBody PallierType pallierType) {
        if (services.upgrade(username, pallierType)) {
            return pallierType;
        }
        else {
            return null;
        }
    }

    @PutMapping(value = "upgrade", produces = {"application/xml", "application/json"} )
    public PallierType angelUpgrade(
            @RequestHeader(value = "X-User", required = false) String username,
            @RequestBody PallierType angelUpgrade) {
        if (services.angelUpgrade(username, angelUpgrade)) {
            return angelUpgrade;
        }
        else {
            return null;
        }
    }

    @DeleteMapping(name="world", produces = {"application/xml", "application/json"})
    public ResponseEntity<World> deleteWorld (@RequestHeader(value = "X-User", required = false) String username) {
        services.deleteWorld(username);
        return ResponseEntity.ok(services.getWorld(username));
    }

}