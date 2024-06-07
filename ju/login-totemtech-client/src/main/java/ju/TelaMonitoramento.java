package ju;

import com.github.britooo.looca.api.core.Looca;
import controller.*;
import dao.MemoriaDAO;
import entities.*;
import entities.register.CpuRegistro;
import entities.register.DiscoRegistro;
import entities.register.MemoriaRegistro;
import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;
import service.Convertions;
import shell.PowerShell;
import shell.TerminalLinux;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class TelaMonitoramento extends JFrame {

    static Totem logged = null;
    static Interrupcoes interrupcoes = null;
    static Cpu cpu = null;
    static Memoria memoria = null;
    static List<Disco> discos = null;
    static int system;
    static Looca looca = new Looca();


    private JLabel inicioMonitoramento;
    private JLabel fimMonitoramento;
    private JLabel memoriaLabel;
    private JLabel discoLabel;
    private JLabel cpuLabel;
    private JLabel processosLabel;
    private JLabel redeLabel;


    public TelaMonitoramento() {

        super("Totem Tech | Monitoramento");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(10, 1));

        Color backgroundColor = Color.decode("#E8E6E3");
        getContentPane().setBackground(backgroundColor);

        //JLabel label = new JLabel("Iniciando monitoramento...");
//        inicioMonitoramento = new JLabel("Iniciando monitoramento");
//        inicioMonitoramento.setHorizontalAlignment(JLabel.CENTER);
//        panel.add(inicioMonitoramento);

//        Timer timer = new Timer(3000, new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                label.setText("Executando testes de monitoramento");
//                label.setHorizontalAlignment(JLabel.CENTER);
//                monitorar();
//            }
//        });
//        timer.setRepeats(false);
//        timer.start();

        memoriaLabel = new JLabel();
        panel.add(memoriaLabel);

        discoLabel = new JLabel();
        panel.add(discoLabel);

        cpuLabel = new JLabel();
        panel.add(cpuLabel);

        processosLabel = new JLabel();
        panel.add(processosLabel);

        redeLabel = new JLabel();
        panel.add(redeLabel);

        fimMonitoramento = new JLabel();
        panel.add(fimMonitoramento);

        add(panel);

        iniciarMonitoramento();
    }

    private void iniciarMonitoramento() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    monitorar();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, 0, 120000); // Executa a cada 2 minutos
    }


    public void monitorar() throws Exception {
        verificarSo();

        if (logged != null) {
            if (system == 1) {
                PowerShell prompt = new PowerShell();
//            prompt.restart();
                prompt.executePowerShellCommand("cls");
            } else {
                TerminalLinux prompt = new TerminalLinux();
                prompt.executeLinuxCommand("clear");
            }
            try {
                cpu = CpuController.getCpu(logged.getIdTotem());
                if (!(cpu != null)) {
                    cpu = new Cpu("Ghz", (CpuController.getFrequenciaProcessador()), 1);
                    CpuController.insertCpu(cpu, logged.getIdTotem());
                    cpu = CpuController.getCpu(logged.getIdTotem());
                }

                memoria = MemoriaController.getMemoria(logged.getIdTotem());
                if (!(memoria != null)) {
                    memoria = new Memoria(MemoriaController.getTotal(), "Gb", 1);
                    MemoriaDAO.insertMemoria(memoria, logged.getIdTotem());
                }

                discos = DiscoController.getDiscos(logged.getIdTotem());
                if (!(discos != null)) {
                    List<com.github.britooo.looca.api.group.discos.Disco> d = DiscoController.getDiscosLooca();
                    for (int i = 0; i < d.size(); i++) {
                        Disco di = new Disco(looca.getGrupoDeDiscos().getDiscos().get(i).getModelo(), Convertions.toDoubleTwoDecimals(Convertions.bytesParaGb(looca.getGrupoDeDiscos().getDiscos().get(i).getTamanho().doubleValue())), "Gb", logged.getIdTotem());
                        DiscoController.insertDisco(di);
                    }
                    Thread.sleep(3000);
                    discos = DiscoController.getDiscos(logged.getIdTotem());
                }

                inicioMonitoramento.setText("Iniciando monitoramento...");
                //System.out.println("Iniciando monitoramento...");
                //Thread.sleep(3000);
            } catch (Exception e) {
                System.out.println("Um erro ocorreu no processo de login, tente novamente mais tarde " + e);
            }

            while (true) {
                inserts();
                inicioMonitoramento.setText("teste2");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

//            Executar a inovação, reiniciar pc
//            if (system == 1) {
//                PowerShell prompt = new PowerShell();
////            prompt.restart();
//                prompt.executePowerShellCommand("restart-computer");
//            } else {
//                TerminalLinux prompt = new TerminalLinux();
//                prompt.executeLinuxCommand("shutdown -r now");
//            }

        }
    }

    public static void verificarSo() {
        String so = System.getProperty("os.name").toLowerCase();

        if (so.contains("win")) {
            system = 1;
        } else {
            system = 2;
        }
    }


    public void inserts() {
        LocalTime inicioTestes = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String horarioFormatado = inicioTestes.format(formatter);

        inicioMonitoramento.setText("Executando testes de monitoramento - " + horarioFormatado);
        //System.out.println("Executando testes de monitoramento - " + horarioFormatado);

        MemoriaRegistro memoriaRegistro = new MemoriaRegistro();
        memoriaRegistro.setTotem(logged.getIdTotem());
        memoriaRegistro.setMemoria(memoria.getIdmemoria());
        memoriaRegistro.setValor(MemoriaController.getUsingPercentage(memoria.getTotal()));

        memoriaLabel.setText("Memoria RAM sendo utilizada em porcentagem: " + memoriaRegistro.getValor());
        //System.out.println("Memoria RAM sendo utilizada em porcentagem: " + memoriaRegistro.getValor());
        MemoriaController.insertMemoriaRegistro(memoriaRegistro);

        List<DiscoRegistro> discosRegistro = new ArrayList<>();
        for (int i = 0; i < discos.size(); i++) {
            discosRegistro.add(new DiscoRegistro());
            discosRegistro.get(i).setDisco(discos.get(i).getIddisco());
            discosRegistro.get(i).setTotem(logged.getIdTotem());
            discosRegistro.get(i).setValor(DiscoController.getUtilizadoPercentage(discos.get(i).getTotal(), i));

            discoLabel.setText("Disco " + i+1 + " espaço utilizado em porcentagem: " + discosRegistro.get(i).getValor());
            //System.out.println("Disco " + i+1 + " espaço utilizado em porcentagem: " + discosRegistro.get(i).getValor());
            DiscoController.insertDiscoRegistro(discosRegistro.get(i));
        }

        CpuRegistro cpuRegistro = new CpuRegistro();
        cpuRegistro.setCpu(cpu.getIdCpu());
        cpuRegistro.setTotem(logged.getIdTotem());
        cpuRegistro.setUtilizacao(CpuController.getUsingPercentage());


        cpuLabel.setText("Cpu sendo utilizada em porcentagem: "+ cpuRegistro.getUtilizacao());
        //System.out.println("Cpu sendo utilizada em porcentagem: " + cpuRegistro.getUtilizacao());
        cpuRegistro.setProcessos(CpuController.getProcessos());

        processosLabel.setText("Total de processos: "+ cpuRegistro.getProcessos());
        //System.out.println("Total de processos: " + cpuRegistro.getProcessos());
        CpuController.insertCpuRegistro(cpuRegistro);

        Rede rede = new Rede();
        rede.setTotem(logged.getIdTotem());
        SpeedTestSocket speedTestSocket = new SpeedTestSocket();
        speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {
            @Override
            public void onCompletion(SpeedTestReport report) {
                redeLabel.setText("Velocidade da rede em mb/s: " + Convertions.toDoubleTwoDecimals(report.getTransferRateBit().divide(new BigDecimal(1000000)).doubleValue()));
                //System.out.println("Velocidade da rede em mb/s: " + Convertions.toDoubleTwoDecimals(report.getTransferRateBit().divide(new BigDecimal(1000000)).doubleValue()));

                rede.setDownload(Convertions.toDoubleTwoDecimals(report.getTransferRateBit().divide(new BigDecimal(1000000)).doubleValue()));
                RedeController.insertRede(rede);

                LocalTime fimTestes = LocalTime.now();
                String horarioFormatado = fimTestes.format(formatter);

                fimMonitoramento.setText("Monitoramento finalizado em " + horarioFormatado + " os componentes passarão por testes de monitoramento novamente em aproximadamente 2 minutos");
//                System.out.println("Monitoramento finalizado em " + horarioFormatado + " os componentes passarão por testes de monitoramento novamente em aproximadamente 2 minutos");
//                System.out.println();
            }

            @Override
            public void onError(SpeedTestError speedTestError, String errorMessage) {
                System.out.println("Totem sem conexão de rede");
                rede.setDownload(null);
                RedeController.insertRede(rede);

                LocalTime fimTestes = LocalTime.now();
                String horarioFormatado = fimTestes.format(formatter);
                System.out.println("Monitoramento finalizado em " + horarioFormatado + " os componentes passarão por testes de monitoramento novamente em aproximadamente 2 minutos");
                System.out.println();
            }

            @Override
            public void onProgress(float percent, SpeedTestReport report) {
                // System.out.println("Progresso teste de internet : " + percent + "%");
            }
        });
        speedTestSocket.startDownload("https://link.testfile.org/15MB");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TelaMonitoramento telaMonitoramento = new TelaMonitoramento();
            telaMonitoramento.setVisible(true);
        });
    }
}
