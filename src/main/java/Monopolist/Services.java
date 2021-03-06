package Monopolist;

import com.sun.xml.bind.v2.TODO;
import generated.*;
import org.glassfish.hk2.runlevel.internal.WouldBlockException;

import javax.validation.constraints.NotNull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

public class Services {

    String filePath = "src/main/resources/";

    public World readWorldFromXml(String username) {
        World world;
        try {
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Unmarshaller u = cont.createUnmarshaller();
            File file = new File(filePath+username+"-world.xml");
            if (file.exists()) {
                world = (World) u.unmarshal(file);
            } else {
                InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
                world = (World) u.unmarshal(input);
                assert input != null;
                input.close();
            }
            return world;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return new World();
    }

    public void saveWordlToXml(World world, String username) {
        try {
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Marshaller m = cont.createMarshaller();
            File file = new File(filePath+username+"-world.xml");
            if (file.exists()) {
                m.marshal(world, file);
            } else {
                OutputStream output = new FileOutputStream(filePath+username+"-world.xml");
                m.marshal(world, output);
                output.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public World getWorld(String username) {
        World world = readWorldFromXml(username);
        updateScoreUSer(world);
        saveWordlToXml(world, username);
        return world;
    }

    /* prend en param??tre le pseudo du joueur et le produit
     * sur lequel une action a eu lieu (lancement manuel de production ou
     * achat d???une certaine quantit?? de produit)
    */
    // renvoie false si l???action n???a pas pu ??tre trait??e
    public Boolean updateProduct(String username, ProductType newproduct) {
        World world = getWorld(username);
        ProductType product = findProductById(world, newproduct.getId());
        if (product == null) { return false;}
        int qtchange = newproduct.getQuantite() - product.getQuantite();
        if (qtchange > 0) {
            double money = world.getMoney();
            double productPrice = newproduct.getCout();
            double productCroissance = newproduct.getCroissance();
            double coutNProduct = productPrice * (1 - Math.pow(productCroissance, qtchange))/ (1 - productCroissance);
            world.setMoney(money - coutNProduct);
            product.setCout(newproduct.getCout());
            product.setQuantite(newproduct.getQuantite());

            PalliersType palliersType = product.getPalliers();
            List<PallierType> pallierTypeList = palliersType.getPallier();
            for (PallierType pallierType : pallierTypeList){
                if (product.getQuantite() >= pallierType.getSeuil() && !pallierType.isUnlocked()){
                    pallierType.setUnlocked(true);
                    if (pallierType.getTyperatio() == TyperatioType.GAIN){
                        product.setRevenu(product.getRevenu() * pallierType.getRatio());
                    }
                    else if (pallierType.getTyperatio() == TyperatioType.VITESSE){
                        product.setVitesse((int) (product.getVitesse() / pallierType.getRatio()));
                        product.setTimeleft((long) (product.getTimeleft() / pallierType.getRatio()));
                    }
                }
            }
        } else {
            product.setTimeleft(product.getVitesse());
        }
        world.setLastupdate(System.currentTimeMillis());
        saveWordlToXml(world, username);
        return true;
    }

    public ProductType findProductById(World world, int id) {
        List<ProductType> productTypeList = world.getProducts().getProduct();
        for (ProductType productType : productTypeList) {
            if (productType.getId() == id) {
                return productType;
            }
        }
        return null;
    }

    /* prend en param??tre le pseudo du joueur et le manager achet??.
     * renvoie false si l???action n???a pas pu ??tre trait??e
     */
    public Boolean updateManager(String username, PallierType newmanager) {
        World world = getWorld(username);
        PallierType manager = findManagerByName(world, newmanager.getName());
        if (manager == null) { return false; }
        manager.setUnlocked(true);
        ProductType product = findProductById(world, manager.getIdcible());
        if (product == null) { return false; }
        product.setManagerUnlocked(true);
        world.setMoney(world.getMoney() - manager.getSeuil());
        world.setLastupdate(System.currentTimeMillis());
        saveWordlToXml(world, username);
        return true;
    }

    public PallierType findManagerByName(World world, String name) {
        List<PallierType> pallierTypeList = world.getManagers().getPallier();
        for (PallierType pallierType : pallierTypeList) {
            if (pallierType.getName().equals(name)) {
                return pallierType;
            }
        }
        return null;
    }

    public void updateScoreUSer(@NotNull World world) {
        List<ProductType> productTypeList = world.getProducts().getProduct();
        long lastUpdate = System.currentTimeMillis() - world.getLastupdate();
        double addScore = 0;

        for (ProductType productType : productTypeList) {
            if (!productType.isManagerUnlocked()) {
                if ((productType.getTimeleft() != 0) && (productType.getTimeleft() <= lastUpdate)) {
                    addScore += productType.getRevenu() * productType.getQuantite() *
                            (1 + world.getActiveangels() * world.getAngelbonus() / 100);
                    productType.setTimeleft(0);
                } else if (productType.getTimeleft() > 0) {
                    productType.setTimeleft(productType.getTimeleft() - lastUpdate);
                }
            } else {
                long nbProduct = lastUpdate / productType.getVitesse();
                if (nbProduct > 0){
                    addScore += (
                            nbProduct +productType.getQuantite()) * productType.getRevenu() *
                            (1 + world.getActiveangels() * world.getAngelbonus() / 100);
                } else{
                    productType.setTimeleft(productType.getTimeleft() - lastUpdate);
                }
            }
        }
        world.setLastupdate(System.currentTimeMillis());
        world.setScore(world.getScore() + addScore);
        world.setMoney(world.getMoney() + addScore);
    }

    public PallierType findUpgradeByName(World world, String name) {
        List<PallierType> pallierTypeList = world.getUpgrades().getPallier();
        for (PallierType upgrade : pallierTypeList) {
            if (upgrade.getName().equals(name)) {
                return upgrade;
            }
        }
        return null;
    }

public boolean upgrade(String username, PallierType newUpgrade){
        World world = getWorld(username);
        PallierType upgrade = findUpgradeByName(world, newUpgrade.getName());
        if (newUpgrade.getIdcible() == 0){
            for (ProductType productType: world.getProducts().getProduct()){
                if (upgrade.getTyperatio() == TyperatioType.VITESSE){
                    productType.setVitesse((int) (productType.getVitesse() / upgrade.getRatio()));
                    productType.setTimeleft((long) (productType.getTimeleft() / upgrade.getRatio()));
                }
                else if (upgrade.getTyperatio() == TyperatioType.GAIN){
                    productType.setRevenu(productType.getRevenu() * upgrade.getRatio());
                }
            }
        }
        else{
            ProductType product = findProductById(world, newUpgrade.getIdcible());
            if (product == null){return false;}
            if (upgrade.getTyperatio() == TyperatioType.VITESSE){
                product.setVitesse((int) (product.getVitesse() / upgrade.getRatio()));
                product.setTimeleft((long) (product.getTimeleft() / upgrade.getRatio()));
            }
            else if (upgrade.getTyperatio() == TyperatioType.GAIN){
                product.setRevenu(product.getRevenu() * upgrade.getRatio());
            }
        }
        upgrade.setUnlocked(true);
        world.setMoney(world.getMoney() - upgrade.getSeuil());
        upgrade.setRatio(1);
        world.setLastupdate(System.currentTimeMillis());
        saveWordlToXml(world, username);
        return true;
    }

    /*
    public boolean angelUpgrade(String name, PallierType angelupgrades) {
        return true;
    }
*/
    public void deleteWorld(String username) {
        World world;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(World.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
            World newWorld = (World) jaxbUnmarshaller.unmarshal(input);
            assert input != null;
            input.close();

            File f = new File(filePath + username + "-world.xml");
            world = (World) jaxbUnmarshaller.unmarshal(f);

            newWorld.setScore(world.getScore());
            double nbAnges = 150 * Math.sqrt(world.getScore() / Math.pow(10,15)) - world.getTotalangels();
            newWorld.setActiveangels(nbAnges);
            newWorld.setTotalangels(nbAnges);
            saveWordlToXml(newWorld, username);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
