package ju;

import javax.swing.*;
import java.awt.*;

public class TelaInicio extends JFrame {
    public TelaInicio() {

        super("Totem Tech | Início");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 300);
        setLocationRelativeTo(null);


        Color backgroundColor = Color.decode("#E8E6E3");
        Color buttonColor = Color.decode("#9B8A85");

        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.setBackground(backgroundColor);

        JLabel label = new JLabel("Bem-vindo(a) à Totem Tech!");
        label.setHorizontalAlignment(JLabel.CENTER);
        panel.add(label);

        JButton iniciarButton = new JButton("Iniciar");
        iniciarButton.setPreferredSize(new Dimension(10, 40));
        iniciarButton.setBackground(buttonColor);

        iniciarButton.addActionListener(e -> {
            new TelaLogin().setVisible(true); //chama a tela de login
            dispose(); //fecha a tela de inicio
        });

        panel.add(iniciarButton);
        add(panel, BorderLayout.CENTER);
    }
}

