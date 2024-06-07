package ju;

import controller.TotemController;
import entities.Totem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TelaLogin extends JFrame {
    private JTextField inputEmailTotem;
    private JPasswordField inputSenha;

    public TelaLogin() {
        //conf da tela login
        super("Totem tech | Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 300);
        setLocationRelativeTo(null);

        Color backgroundColor = Color.decode("#E8E6E3");
        Color buttonColor = Color.decode("#679BC5");

        getContentPane().setBackground(backgroundColor);

        JPanel panel = new JPanel(new GridLayout(6, 1));
        panel.setBackground(backgroundColor);
        panel.setBorder(new EmptyBorder(15, 10, 10, 15));

        JLabel label = new JLabel("Realize o login de seu totem:");
        label.setHorizontalAlignment(JLabel.CENTER);
        panel.add(label);

        //login
        JLabel emailTotem = new JLabel("Email: ");
        emailTotem.setHorizontalAlignment(JLabel.LEFT);
        panel.add(emailTotem);

        inputEmailTotem = new JTextField();
        inputEmailTotem.setHorizontalAlignment(JTextField.LEFT);
        panel.add(inputEmailTotem);

        //senha
        JLabel senhaTotem = new JLabel("Senha: ");
        senhaTotem.setHorizontalAlignment(JLabel.LEFT);
        panel.add(senhaTotem);

        inputSenha = new JPasswordField();
        inputSenha.setHorizontalAlignment(JPasswordField.LEFT);
        panel.add(inputSenha);


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(new JLabel());

        JButton buttonLogin = new JButton("Login");
        buttonLogin.setBackground(buttonColor);

        buttonLogin.addActionListener(e -> {
            try {
                entrar();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro durante o login: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(buttonLogin);

        JButton button = new JButton("Voltar");
        button.setBackground(buttonColor);

        button.addActionListener(e -> {
            voltar();
        });
        buttonPanel.add(button);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
    }

    public void entrar() throws Exception {
        String inputEmail = inputEmailTotem.getText();
        char[] senhaCharArray = inputSenha.getPassword();
        String senha = new String(senhaCharArray);

        try {
            Totem totemLog = TotemController.login(inputEmail, senha);
            if (totemLog != null) {
                //fechar a tela e focar no cmd
                setVisible(false);
                JOptionPane.showMessageDialog(this, "Login realizado com sucesso");

                System.out.println("""
                *------------------------------------*
                |        Login - Totem Tech          |
                *------------------------------------*
                | Login realizado com sucesso!       |
                *------------------------------------* """);

                //inicia o monitoramento
                new Thread(() -> {
                    try {
                        App app = new App();
                        app.iniciarMonitoramento();
                        System.out.println("teste ");
                    } catch (Exception ex) {
                        System.out.println("caiu no catch ");
                        ex.printStackTrace();
                    }


                }).start();

            } else {
                int opcao;
                do {
                    setVisible(false);

                    opcao = JOptionPane.showOptionDialog(this,
                            "Usuário não encontrado!",
                            "Erro",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.ERROR_MESSAGE,
                            null,
                            new String[]{"Tentar novamente", "Sair"},
                            null);

                    switch (opcao) {
                        case JOptionPane.YES_OPTION:
                            inputEmailTotem.setText("");
                            inputSenha.setText("");

                            setVisible(true);

                            break;
                        case JOptionPane.NO_OPTION:
                            System.exit(0);
                            break;
                    }
                } while (opcao != JOptionPane.YES_OPTION && opcao != JOptionPane.NO_OPTION);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Erro durante o login: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }


    private void voltar() {
        dispose();
        new TelaInicio().setVisible(true);
    }

//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            TelaSeguinte telaSeguinte = new TelaSeguinte();
//            telaSeguinte.setVisible(true);
//        });
//    }

}
