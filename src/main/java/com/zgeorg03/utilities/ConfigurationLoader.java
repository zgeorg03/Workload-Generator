package com.zgeorg03.utilities;

import com.zgeorg03.models.Operation;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ConfigurationLoader {
    public static Configuration load(String s) throws FileNotFoundException {
        Yaml yaml = new Yaml();
        InputStream in = new FileInputStream(new File(s));
        Configuration configuration = yaml.loadAs(in,Configuration.class);
        return configuration;
    }
}
