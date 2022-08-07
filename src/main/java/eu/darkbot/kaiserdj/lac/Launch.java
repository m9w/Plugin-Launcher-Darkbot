package eu.darkbot.kaiserdj.lac;

import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.annotations.Option;
import eu.darkbot.api.extensions.*;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.managers.BackpageAPI;
import eu.darkbot.api.utils.Inject;
import eu.darkbot.util.Popups;
import eu.darkbot.util.SystemUtils;

import java.io.File;
import java.nio.file.Files;

import javax.swing.*;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

@RegisterFeature
@Feature(name = "Launch", description = "Open the account you are using at that moment in DarkOrbit Client (with Dosid)")
public class Launch implements Task, InstructionProvider, Configurable<Launch.Config>, ExtraMenus {
    BackpageAPI backpage;

    @Inject
    public Launch(BackpageAPI backpage){
        this.backpage = backpage;
    }

    @Override
    public void onTickTask() { }

    public JComponent beforeConfig() {
        final JButton tutorial = new JButton("Tutorial");
        tutorial.addActionListener(e -> {
            JButton github = new JButton("Download client");
            github.addActionListener(x -> SystemUtils.openUrl("https://github.com/kaiserdj/Darkorbit-client/releases/latest"));

            JButton detectClient = new JButton("Automatic client detection");
            detectClient.addActionListener((a -> {
                String client = System.getenv("LOCALAPPDATA") + "\\Programs\\darkorbit-client\\DarkOrbit Client.exe";

                if (Files.exists(Paths.get(client))) {
                    this.config.CUSTOM_FILE = new File(client);
                    Popups.showMessageAsync("Client detected", "The client has been detected and the location saved.", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    Popups.showMessageAsync("Error", "The client has not been detected.\n" +
                            "Please check that you have the client installed and try again.\n" +
                            "If you keep getting error, please enter the client's .exe location manually", JOptionPane.ERROR_MESSAGE);
                }
            }));

            JButton video = new JButton("Video-tutorial");
            video.addActionListener(x -> SystemUtils.openUrl("https://vimeo.com/501184515"));

            Object[] options = {
                    "Basic guide to configure the plugin:",
                    " ",
                    "Download and install the client",
                    github,
                    " ",
                    "Try to automatically detect the client's .exe location",
                    detectClient,
                    " ",
                    "Manual way to configure plugin",
                    "1º Obtain the location of the client's .exe file",
                    "2º Select in the option \"Launcher.exe\" of the plugin the file \"DarkOrbit Client.exe\"",
                    video
            };

            Popups.showMessageAsync("Tutorial", options, JOptionPane.INFORMATION_MESSAGE);
        });

        return tutorial;
    }

    public static class Config {
        @Option(value = "Launcher.exe"/*, description = "Select the location of the launcher .exe file"*/)
        public File CUSTOM_FILE;
    }

    private Config config;

    @Override
    public void setConfig(ConfigSetting<Config> config) {
        this.config = config.getValue();
    }

    @Override
    public Collection<JComponent> getExtraMenuItems(PluginAPI pluginAPI){
        return Arrays.asList(
                createSeparator("Launch"),
                create("Open client", e -> {
                            String sid = backpage.getSid(), instance = backpage.getInstanceURI().toString();
                            if (sid == null || sid.isEmpty() || instance == null || instance.isEmpty()) {
                                Popups.showMessageAsync(
                                        "Error",
                                        "Error getting user data",
                                        JOptionPane.INFORMATION_MESSAGE);

                                return;
                            }
                            String[] command = new String[] {this.config.CUSTOM_FILE.getAbsolutePath(), "--dosid", instance + "?dosid=" + sid};

                            try {
                                Class.forName("com.github.manolo8.darkbot.utils.RuntimeUtil")
                                        .getMethod("execute", String[].class)
                                        .invoke(null, command);
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                        }
                ));
    }
}
