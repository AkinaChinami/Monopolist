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
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        // trouver dans ce monde, le produit équivalent à celui passé en paramètre
        ProductType product = findProductById(world, newproduct.getId());
        if (product == null) { return false;}
        // calculer la variation de quantité. Si elle est positive c'est
        // que le joueur a acheté une certaine quantité de ce produit
        // sinon c’est qu’il s’agit d’un lancement de production.
        int qtchange = newproduct.getQuantite() - product.getQuantite();
        if (qtchange > 0) {
            // soustraire de l'argent du joueur le cout de la quantité
            // achetée et mettre à jour la quantité de product
            double money = world.getMoney();
            double productPrice = newproduct.getCout();
            int productQuantite = newproduct.getQuantite();
            double productCroissance = newproduct.getCroissance();

            world.setMoney(money - ((Math.pow(productPrice,productCroissance)/productCroissance)*productQuantite));
            product.setQuantite(product.getQuantite() + newproduct.getQuantite());
        } else {
            // initialiser product.timeleft à product.vitesse pour lancer la production
            product.setTimeleft(product.getVitesse());
        }
        // sauvegarder les changements du monde
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
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        // trouver dans ce monde, le manager équivalent à celui passé en paramètre
        PallierType manager = findManagerByName(world, newmanager.getName());
        if (manager == null) { return false; }
        // débloquer ce manager
        manager.setUnlocked(true);
        // trouver le produit correspondant au manager
        ProductType product = findProductById(world, manager.getIdcible());
        if (product == null) { return false; }
        // débloquer le manager de ce produit
        product.setManagerUnlocked(true);
        // soustraire de l'argent du joueur le cout du manager
        double money = world.getMoney();
        world.setMoney(money - manager.getSeuil());
        // sauvegarder les changements au monde
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
        for (ProductType productType : productTypeList) {
            if (!productType.isManagerUnlocked()) {
                if ((productType.getTimeleft() != 0) && (productType.getTimeleft() < lastUpdate)) {
                    world.setScore(world.getScore() + productType.getQuantite()*productType.getRevenu());
                } else {
                    productType.setTimeleft(productType.getTimeleft() - lastUpdate);
                }
            } else {
                long nbProduct = lastUpdate / productType.getVitesse();
                world.setScore(world.getScore() + nbProduct*productType.getRevenu());
            }
        }
        world.setLastupdate(System.currentTimeMillis());
    }
/*
    public void upgrade(String name, PallierType upgrade) {

    }*/
}
