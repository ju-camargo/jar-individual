package slack;

import com.google.gson.JsonObject;
import org.json.JSONObject;
import repository.local.LocalDatabaseConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import repository.remote.RemoteDatabaseConnection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnvioAlertas {
    private JdbcTemplate jbdcTemplate;
    private Slack slack;

    private LocalDateTime ultimoAlertaComponente;
    private LocalDateTime ultimoAlertaInterrupcao;

    static List<Boolean> memoriaError = new ArrayList<>();
    static List<Boolean> cpuError = new ArrayList<>();

    public EnvioAlertas(RemoteDatabaseConnection conexaoDoBanco, Slack slack) {
        this.jbdcTemplate = conexaoDoBanco.getConexaoDoBanco();
        this.slack = slack;
    }

    public void verificarDados() throws Exception{
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate = LocalDate.now();
            String formattedDate = localDate.format(dtf);


            LocalDate data = LocalDate.now();
            LocalDateTime ultimoHorarioProcessado = data.atStartOfDay();


            String consultaInterrupcoes = "SELECT TOP 1 t.nome AS nomeTotem, i.motivo, i.horario FROM interrupcoes AS i JOIN totem AS t ON i.totem = t.idtotem WHERE CAST(i.horario AS DATE) = '" + formattedDate + "' AND i.horario > ? ORDER BY i.horario DESC;";
            List<Map<String, Object>> interrupcoes = jbdcTemplate.queryForList(consultaInterrupcoes, ultimoHorarioProcessado);
            for (Map<String, Object> interrupcao : interrupcoes) {
                LocalDateTime horarioInterrupcao = (LocalDateTime) interrupcao.get("horario");
                String nomeTotem = (String) interrupcao.get("nomeTotem");
                String motivoInterrupcao = (String) interrupcao.get("motivo");

                if (ultimoAlertaInterrupcao == null || horarioInterrupcao.isAfter(ultimoAlertaInterrupcao)) {
                    if (interrupcao != null && !interrupcao.isEmpty()) {

                        if (horarioInterrupcao != null && nomeTotem != null && motivoInterrupcao != null) {
                            JSONObject jsonAlertaInterrupcao = new JSONObject();
                            jsonAlertaInterrupcao.put("text", """
                                    *Alerta: Totem Reiniciado!*
                                            
                                    *Detalhes:*
                                    - Totem: `%s`
                                    - Motivo: `%s`
                                    - Data/Hora: `%s`
                                            
                                            
                                    :information_source: | Verifique se o totem voltou a funcionar corretamente e monitore para possíveis problemas recorrentes.""".formatted(nomeTotem, motivoInterrupcao, horarioInterrupcao));

                            slack.sendSlackMessage(System.getenv("token-interrupcoes"), jsonAlertaInterrupcao);
                        }
                    }
                    ultimoAlertaInterrupcao = horarioInterrupcao;
                }
            }

            //monitoramento dos componentes

            String consulta = "SELECT t.nome AS nomeTotem, c.nome AS nomeComponente, tc.nome AS tipoComponente, r.valor, r.horario FROM totem AS t JOIN componente AS c ON t.idtotem = c.totem JOIN tipoComponente AS tc ON tc.idtipoComponente = c.tipo JOIN registro AS r ON r.componente = c.idcomponente WHERE CAST(r.horario AS DATE) = '" + formattedDate + "' AND r.valor LIKE '%.%' ORDER BY r.horario DESC;";
            List<RegistroComponentes> registros = jbdcTemplate.query(consulta, (rs, rowNum) -> new RegistroComponentes(
                    rs.getString("nomeTotem"),
                    rs.getString("nomeComponente"),
                    rs.getString("tipoComponente"),
                    rs.getDouble("valor"),
                    rs.getTimestamp("horario").toLocalDateTime()
            ));

            for (RegistroComponentes registro : registros){
                if (ultimoAlertaComponente == null || registro.getHorario().isAfter(ultimoAlertaComponente)) {
                    JSONObject json = new JSONObject();
                    switch (registro.getTipoComponente()){
                        case "CPU":
                            if (registro.getValor() < 80){
                                cpuError.add(false);
                            } else if(registro.getValor() >= 80 && registro.getValor() <= 90){
                                cpuError.add(false);
                                json.put("text", """
                                *Alerta: Uso de CPU Elevado!*
                                
                                *Detalhes:*
                                - *Totem:* `%s`
                                - *Valor (CPU):* `%.0f%%`
                                - *Data/Hora:* `%s`
                                
                                
                                :warning: | CPU está sendo utilizada com eficiência, mas pode haver lentidão em momentos de pico.""".formatted(registro.getNomeTotem(), registro.getValor(), registro.getHorario()));

                                sendSlackMessage(System.getenv("token-alertas"), json);
                            } else if (registro.getValor() > 90) {
                                if (cpuError.isEmpty()){
                                    cpuError.add(true);
                                    json.put("text", """
                                *Crítico: sobrecarga de CPU!*
                                
                                *Detalhes:*
                                - Totem: `%s`
                                - Valor (CPU): `%.0f%%`
                                - Data/hora: `%s`
                                
                                *Caso o problema persista o totem será reiniciado!!!*
                                
                                :rotating_light: | Sobrecarga da CPU pode resultar em lentidão, travamentos e instabilidades do sistema.""".formatted(registro.getNomeTotem(), registro.getValor(), registro.getHorario()));

                                    sendSlackMessage(System.getenv("token-criticos"), json);
                                } else if (!cpuError.get(cpuError.size() - 1)) {
                                    cpuError.add(true);

                                    json.put("text", """
                                *Crítico: sobrecarga de CPU!*
                                
                                *Detalhes:*
                                - Totem: `%s`
                                - Valor (CPU): `%.0f%%`
                                - Data/hora: `%s`
                                
                                *Caso o problema persista o totem será reiniciado!!!*
                                
                                :rotating_light: | Sobrecarga da CPU pode resultar em lentidão, travamentos e instabilidades do sistema.""".formatted(registro.getNomeTotem(), registro.getValor(), registro.getHorario()));

                                    sendSlackMessage(System.getenv("token-criticos"), json);
                                }
                            }
                            break;
                        case "Disco":
                            if (registro.getValor() > 80 && registro.getValor() <= 90){
                                json.put("text", """
                                *Alerta: Atenção a utilização do disco!*
                                
                                *Detalhes:*
                                - Totem: `%s`
                                - Valor (disco): `%.0f`
                                - Data/hora: `%s`
                                
                                
                                :warning: | Nível de alerta que exige monitoramento para evitar que a utilização do disco exceda a capacidade.""".formatted(registro.getNomeTotem(), registro.getValor(), registro.getHorario()));

                                sendSlackMessage(System.getenv("token-alertas"), json);
                            } else if (registro.getValor() > 90) {
                                json.put("text", """
                                *Crítico: utilização excessiva do disco!*
                                
                                *Detalhes:*
                                - Totem: `%s`
                                - Valor (disco): `%.0f`
                                - Data/hora: `%s`
                                
                                
                                :rotating_light: | Utilização excessiva do disco pode levar a lentidão, travamentos e falhas no sistema.""".formatted(registro.getNomeTotem(), registro.getValor(), registro.getHorario()));

                                sendSlackMessage(System.getenv("token-criticos"), json);
                            }
                            break;
                        case "Memória":
                            if (registro.getValor() > 85){
                                memoriaError.add(false);
                            } else if(registro.getValor() >= 85 && registro.getValor() <= 89){
                                memoriaError.add(false);
                                json.put("text", """
                                *Alerta: Atenção a utilização da memória!*
                                
                                *Detalhes:*
                                - Totem: `%s`
                                - Valor (memória): `%.0f`
                                - Data/hora: `%s`
                                
                                
                                :warning: | Nível aceitável de memória, mas exige monitoramento para evitar sobrecarga da memória.""".formatted(registro.getNomeTotem(), registro.getValor(), registro.getHorario()));

                                sendSlackMessage(System.getenv("token-alertas"), json);
                            } else if (registro.getValor() > 89) {
                                if (memoriaError.isEmpty()){
                                    memoriaError.add(true);
                                    json.put("text", """
                                    *Crítico: sobrecarga da memória!*
                                    
                                    *Detalhes:*
                                    - Totem: `%s`
                                    - Valor (memória): `%.0f`
                                    - Data/hora: `%s`
                                    
                                    *Caso o problema persista o totem será reiniciado!!!*
                                    
                                    :rotating_light: | Sobrecarga da memória pode levar a lentidão, travamentos, falhas no sistema e até mesmo perda de dados.""".formatted(registro.getNomeTotem(), registro.getValor(), registro.getHorario()));

                                    sendSlackMessage(System.getenv("token-criticos"), json);
                                } else if (!memoriaError.get(memoriaError.size() - 1)){
                                    memoriaError.add(true);
                                    json.put("text", """
                                    *Crítico: sobrecarga da memória!*
                                    
                                    *Detalhes:*
                                    - Totem: `%s`
                                    - Valor (memória): `%.0f`
                                    - Data/hora: `%s`
                                    
                                    *Caso o problema persista o totem será reiniciado!!!*
                                    
                                    :rotating_light: | Sobrecarga da memória pode levar a lentidão, travamentos, falhas no sistema e até mesmo perda de dados.""".formatted(registro.getNomeTotem(), registro.getValor(), registro.getHorario()));

                                    sendSlackMessage(System.getenv("token-criticos"), json);
                                }
                            }
                            break;
                        case "Rede":
                            if (registro.getValor() < 10 && registro.getValor() > 6){
                                json.put("text", """
                                *Alerta: Atenção a velocidade da rede!*
                                
                                *Detalhes:*
                                - Totem: `%s`
                                - Valor (rede): `%.0f MB/s`
                                - Data/hora: `%s`
                                
                                
                                :warning: | O sistema funcionará sem problemas, porém, pode apresentar problemas em horário de pico.""".formatted(registro.getNomeTotem(), registro.getValor(), registro.getHorario()));

                                sendSlackMessage(System.getenv("token-alertas"), json);
                            }
                            else if (registro.getValor() < 5){
                                json.put("text", """
                                *Crítico: Rede muito baixa!*
                                
                                *Detalhes:*
                                - Totem: `%s`
                                - Valor (rede): `%.0f MB/s`
                                - Data/hora: `%s`
                                
                                
                                :rotating_light: | Totem pode indicar lentidão, travamento e instabilidade do sistema.""".formatted(registro.getNomeTotem(), registro.getValor(), registro.getHorario()));

                                sendSlackMessage(System.getenv("token-criticos"), json);
                            }
                            break;
                        default:
                            if (registro.getValor() == 0){
                                json.put("text", """
                                Alerta: Totem possívelmente sem rede!!
                                Detalhes:
                                - Totem: `%s`
                                - Última data/hora com rede: `%s`
                                Totem pode indicar lentidão, travamento e instabilidade do sistema.""".formatted(registro.getNomeTotem(), registro.getHorario()));

                                sendSlackMessage(System.getenv("token-alertas"), json);
                            }
                            break;

                    }

                    ultimoAlertaComponente = registro.getHorario();
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void sendSlackMessage(String webhookUrl, JSONObject json) throws Exception {
        slack.sendSlackMessage(webhookUrl, json);
    }
}

