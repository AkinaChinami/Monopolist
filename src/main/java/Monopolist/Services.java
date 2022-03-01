package Monopolist;

import generated.World;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Services {

    public World readWorldFromXml() {
        World world1;
        try {
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Unmarshaller u = cont.createUnmarshaller();
            InputStream input =
                    getClass().getClassLoader().getResourceAsStream("world.xml");
            world1 = (World) u.unmarshal(input);
            return world1;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return new World();
    }

    public void saveWordlToXml(World world) {
        try {
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Marshaller m = cont.createMarshaller();
            OutputStream output = new FileOutputStream("world.xml");
            m.marshal(world, output);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public World getWorld() {
        return readWorldFromXml();
    }
}
