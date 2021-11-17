import org.lwjgl.PointerBuffer;

import javax.swing.*;
import java.awt.*;

import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.util.nfd.NativeFileDialog.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Test dialog");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            JLabel label = new JLabel("no file selected");

            JButton button = new JButton("Click me");
            button.addActionListener(e -> {
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

            frame.add(button, BorderLayout.NORTH);
            frame.add(label, BorderLayout.SOUTH);

            frame.pack();
            frame.setMinimumSize(new Dimension(500, 500));

            frame.setVisible(true);
        });
    }
}
