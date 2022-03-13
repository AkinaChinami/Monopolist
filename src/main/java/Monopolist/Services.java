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

    /* prend en paramètre le pseudo du joueur et le produit
     * sur lequel une action a eu lieu (lancement manuel de production ou
     * achat d’une certaine quantité de produit)
    */
    // renvoie false si l’action n’a pas pu être traitée
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

    /* prend en paramètre le pseudo du joueur et le manager acheté.
     * renvoie false si l’action n’a pas pu être traitée
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
                if ((productType.getTimeleft() != 0) && (productType.getTimeleft() < lastUpdate)) {
                    addScore += productType.getRevenu() * productType.getQuantite();
                    productType.setTimeleft(0);
                } else if (productType.getTimeleft() > 0) {
                    productType.setTimeleft(productType.getTimeleft() - lastUpdate);
                }
            } else {
                long nbProduct = lastUpdate / productType.getVitesse();
                if (nbProduct > 0){
                    addScore += (nbProduct + productType.getQuantite()) * productType.getRevenu();
                } else{
                    productType.setTimeleft(productType.getTimeleft() - lastUpdate);
                }
            }
        }
        world.setLastupdate(System.currentTimeMillis());
        world.setScore(world.getScore() + addScore);
        world.setMoney(world.getMoney() + addScore);
    }
/*
    public void upgrade(String name, PallierType upgrade) {

    }*/
}
