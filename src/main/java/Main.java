import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.FileChooser;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.util.nfd.NativeFileDialog.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        try {
            Class.forName("javafx.stage.FileChooser");
            // Initializes JavaFX environment
            //noinspection unused
            JFXPanel jfxPanel = new JFXPanel();
        } catch (ClassNotFoundException ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Test dialog");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            JLabel label = new JLabel("no file selected");

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout());

            frame.add(buttonPanel, BorderLayout.NORTH);
            frame.add(label, BorderLayout.SOUTH);

            JButton button1 = new JButton("Native File Dialog (LWJGL)");
            buttonPanel.add(button1);
            button1.addActionListener(e -> {
                PointerBuffer outPath = memAllocPointer(1);

                String selectedFile = null;
                try {
                    int result = NFD_OpenDialog("ori,cp;ori;cp", null, outPath);

                    if (result == NFD_OKAY) {
                        selectedFile = outPath.getStringUTF8(0);
                        nNFD_Free(outPath.get(0));
                    }
                } finally {
                    memFree(outPath);
                }

                label.setText(selectedFile);
            });

            JButton button2 = new JButton("JFileChooser (Swing)");
            buttonPanel.add(button2);
            button2.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Ori file", "ori"));

                fileChooser.showOpenDialog(frame);

                if (fileChooser.getSelectedFile() != null) {
                    label.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            });

            JButton button3 = new JButton("FileDialog (AWT)");
            buttonPanel.add(button3);
            button3.addActionListener(e -> {
                FileDialog fileDialog = new FileDialog(frame);

                fileDialog.setFile("*.ori,*.cp;*.ori;*.cp");
                fileDialog.setVisible(true);

                if (fileDialog.getFile() != null) {
                    label.setText(fileDialog.getFile());
                }
            });

            JButton button4 = new JButton("FileDialog (JavaFX)");
            buttonPanel.add(button4);
            button4.addActionListener(e -> Platform.runLater(() -> {
                frame.setEnabled(false);

                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Supported file", "*.ori", "*.cp"));
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Ori file", "*.ori"));
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Cp file", "*.cp"));

                File currentFile = fileChooser.showOpenDialog(null);

                if (currentFile != null) {
                    label.setText(currentFile.getAbsolutePath());
                }

                frame.setEnabled(true);
            }));

            JButton button5 = new JButton("TinyFD (LWJGL)");
            buttonPanel.add(button5);
            button5.addActionListener(e -> {
                frame.setEnabled(false);

                MemoryStack stack = MemoryStack.stackPush();

                PointerBuffer filterPatterns = stack.mallocPointer(3);
                filterPatterns.put(stack.UTF8("*.ori"));
                filterPatterns.put(stack.UTF8("*.cp"));

                filterPatterns.flip();

                String file = TinyFileDialogs.tinyfd_openFileDialog("Open File", null, filterPatterns, "Supported files (*.ori, *.cp)", false);

                if (file != null) {
                    label.setText(file);
                }

                stack.pop();

                frame.setEnabled(true);
            });

            frame.pack();
            frame.setMinimumSize(new Dimension(500, 500));

            frame.setVisible(true);
        });
    }
}
