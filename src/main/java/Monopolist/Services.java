package Monopolist;

import generated.ProductType;
import generated.World;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Services {

    String filePath = "src/main/resources";

    public World readWorldFromXml(String username) {
        World world;
        try {
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Unmarshaller u = cont.createUnmarshaller();
            File file = new File(filePath+username+"-world.xml");
            if (file.exists()) {
                world = (World) u.unmarshal(file);
            } else {
                InputStream input = getClass().getClassLoader().getResourceAsStream(filePath+"world.xml");
                world = (World) u.unmarshal(input);
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
                OutputStream output = new FileOutputStream(filePath+username+"world.xml");
                m.marshal(world, output);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public World getWorld(String username) {
        World world = readWorldFromXml(username);
        saveWordlToXml(world, username);
        return world;
    }
}
