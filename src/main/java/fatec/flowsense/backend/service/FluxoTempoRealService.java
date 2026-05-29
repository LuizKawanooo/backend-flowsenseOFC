package fatec.flowsense.backend.service;

import fatec.flowsense.backend.controller.dto.SensorDisponivelDto;
import fatec.flowsense.backend.controller.dto.SensorFluxoDto;

import fatec.flowsense.backend.entities.AlertaVazamento;
import fatec.flowsense.backend.entities.ConsumoDiario;
import fatec.flowsense.backend.entities.Device;

import fatec.flowsense.backend.repository.AlertaVazamentoRepository;
import fatec.flowsense.backend.repository.ConsumoDiarioRepository;
import fatec.flowsense.backend.repository.DeviceRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FluxoTempoRealService {

    private final ConsumoDiarioRepository consumoDiarioRepository;
    private final DeviceRepository deviceRepository;
    private final AlertaVazamentoRepository alertaVazamentoRepository;
    private final PushNotificationService pushNotificationService;

    private final Map<Long, SensorEstado> sensores = new ConcurrentHashMap<>();

    private static final double FLUXO_MINIMO_REAL = 0.1;
    private static final double LIMITE_TORNEIRA_ABERTA = 1.5;
    private static final long TEMPO_FLUXO_BAIXO_SEGUNDOS = 60;
    private static final long TEMPO_TORNEIRA_ABERTA_SEGUNDOS = 60;
    private static final long COOLDOWN_ALERTA_MINUTOS = 0;

    public FluxoTempoRealService(
            ConsumoDiarioRepository consumoDiarioRepository,
            DeviceRepository deviceRepository,
            AlertaVazamentoRepository alertaVazamentoRepository,
            PushNotificationService pushNotificationService
    ) {
        this.consumoDiarioRepository = consumoDiarioRepository;
        this.deviceRepository = deviceRepository;
        this.alertaVazamentoRepository = alertaVazamentoRepository;
        this.pushNotificationService = pushNotificationService;
    }

    public void receberFluxo(SensorFluxoDto dto, Device device) {

        if (dto.getSensorId() == null) {
            return;
        }

        Long sensorId = dto.getSensorId();

        SensorEstado estado = sensores.computeIfAbsent(
                sensorId,
                id -> new SensorEstado(sensorId)
        );

        synchronized (estado) {

            double fluxoAtual = dto.getFluxo() != null ? dto.getFluxo() : 0.0;

            double litrosNoIntervalo = dto.getLitrosSegundo() != null
                    ? dto.getLitrosSegundo()
                    : fluxoAtual / 60.0;

            estado.setNome(device.getName());
            estado.setOwnerEmail(device.getOwnerEmail());
            estado.setFluxoAtualLitrosPorMinuto(fluxoAtual);
            estado.setUltimoSinal(LocalDateTime.now());

            estado.setLitrosPendentesEmMemoria(
                    estado.getLitrosPendentesEmMemoria() + litrosNoIntervalo
            );

            estado.setDailyTotal(
                    estado.getDailyTotal() + litrosNoIntervalo
            );

            estado.setTotalFlow(
                    estado.getTotalFlow() + litrosNoIntervalo
            );

            detectarVazamento(estado, device, fluxoAtual);
        }

        System.out.println("=================================");
        System.out.println("FLUXO RECEBIDO EM MEMORIA");
        System.out.println("Sensor ID: " + sensorId);
        System.out.println("Nome: " + device.getName());
        System.out.println("Dono: " + device.getOwnerEmail());
        System.out.println("Fluxo atual: " + dto.getFluxo() + " L/min");
        System.out.println("Litros no intervalo: " + dto.getLitrosSegundo());
        System.out.println("=================================");
    }

    @Scheduled(fixedRate = 60000)
    public void salvarConsumoPendenteACadaUmMinuto() {

        System.out.println("=================================");
        System.out.println("SALVANDO CONSUMO PENDENTE NO BANCO");
        System.out.println("Intervalo: 1 minuto");
        System.out.println("=================================");

        sensores.forEach((sensorId, estado) -> {

            synchronized (estado) {

                double litrosPendentes = estado.getLitrosPendentesEmMemoria();

                if (litrosPendentes <= 0) {
                    return;
                }

                String ownerEmail = estado.getOwnerEmail();

                if (ownerEmail == null || ownerEmail.isBlank()) {
                    System.out.println("Sensor " + sensorId + " sem ownerEmail. Ignorado.");
                    return;
                }

                LocalDate hoje = LocalDate.now();

                ConsumoDiario consumoDiario = consumoDiarioRepository
                        .findBySensorIdAndOwnerEmailAndDataConsumo(sensorId, ownerEmail, hoje)
                        .orElseGet(() -> {
                            ConsumoDiario novo = new ConsumoDiario();

                            novo.setSensorId(sensorId);
                            novo.setNomeSensor(estado.getNome());
                            novo.setOwnerEmail(ownerEmail);
                            novo.setDataConsumo(hoje);
                            novo.setTotalLitros(0.0);
                            novo.setAtualizadoEm(LocalDateTime.now());

                            return novo;
                        });

                double totalAtual = consumoDiario.getTotalLitros() == null
                        ? 0.0
                        : consumoDiario.getTotalLitros();

                consumoDiario.setNomeSensor(estado.getNome());
                consumoDiario.setOwnerEmail(ownerEmail);
                consumoDiario.setTotalLitros(totalAtual + litrosPendentes);
                consumoDiario.setAtualizadoEm(LocalDateTime.now());

                consumoDiarioRepository.save(consumoDiario);

                estado.setLitrosPendentesEmMemoria(0.0);

                System.out.println("Sensor ID: " + sensorId);
                System.out.println("Dono: " + ownerEmail);
                System.out.println("Litros salvos agora: " + litrosPendentes);
                System.out.println("Total do dia: " + consumoDiario.getTotalLitros());
                System.out.println("---------------------------------");
            }
        });
    }

    public List<SensorDisponivelDto> listarSensoresDisponiveis(String ownerEmail) {

        System.out.println("=================================");
        System.out.println("LISTANDO SENSORES DO USUARIO");
        System.out.println("Owner email recebido: " + ownerEmail);
        System.out.println("=================================");

        List<SensorDisponivelDto> lista = new ArrayList<>();

        List<Device> devices = deviceRepository.findByOwnerEmail(ownerEmail);

        System.out.println("Dispositivos encontrados no banco: " + devices.size());

        for (Device device : devices) {

            System.out.println("Device banco:");
            System.out.println("ID banco: " + device.getId());
            System.out.println("Sensor ID: " + device.getSensorId());
            System.out.println("Nome: " + device.getName());
            System.out.println("Owner: " + device.getOwnerEmail());
            System.out.println("Active: " + device.getActive());
            System.out.println("Reset requested: " + device.getResetRequested());

            if (Boolean.FALSE.equals(device.getActive())) {
                System.out.println("Ignorado porque active=false");
                continue;
            }

            SensorEstado estado = sensores.get(device.getSensorId());

            boolean online = false;

            if (estado != null && estado.getUltimoSinal() != null) {
                online = Duration
                        .between(estado.getUltimoSinal(), LocalDateTime.now())
                        .getSeconds() <= 10;
            }

            lista.add(new SensorDisponivelDto(
                    String.valueOf(device.getSensorId()),
                    device.getName(),
                    "ESP32 / HTTP",
                    online
            ));
        }

        System.out.println("Total enviado para o app: " + lista.size());

        return lista;
    }

    public Map<String, Object> buscarDadosAtuais(Long sensorId) {

        SensorEstado estado = sensores.get(sensorId);

        double fluxoAtual = 0.0;
        double litrosPendentesEmMemoria = 0.0;
        String nome = "Sensor de Fluxo " + sensorId;
        boolean online = false;
        String ownerEmail = null;

        if (estado != null) {
            synchronized (estado) {
                fluxoAtual = estado.getFluxoAtualLitrosPorMinuto();
                litrosPendentesEmMemoria = estado.getLitrosPendentesEmMemoria();
                nome = estado.getNome();
                ownerEmail = estado.getOwnerEmail();

                online = estado.getUltimoSinal() != null &&
                        Duration.between(
                                estado.getUltimoSinal(),
                                LocalDateTime.now()
                        ).getSeconds() <= 10;
            }
        }

        if (ownerEmail == null || ownerEmail.isBlank()) {
            Device device = deviceRepository.findBySensorId(sensorId).orElse(null);

            if (device != null) {
                ownerEmail = device.getOwnerEmail();
                nome = device.getName();
            }
        }

        LocalDate hoje = LocalDate.now();

        double totalHojeSalvo = 0.0;
        double totalMensalSalvo = 0.0;

        if (ownerEmail != null && !ownerEmail.isBlank()) {

            totalHojeSalvo = consumoDiarioRepository
                    .findBySensorIdAndOwnerEmailAndDataConsumo(sensorId, ownerEmail, hoje)
                    .map(ConsumoDiario::getTotalLitros)
                    .orElse(0.0);

            YearMonth mesAtual = YearMonth.now();

            Double mensal = consumoDiarioRepository.somarConsumoMensal(
                    sensorId,
                    ownerEmail,
                    mesAtual.atDay(1),
                    mesAtual.atEndOfMonth()
            );

            totalMensalSalvo = mensal != null ? mensal : 0.0;
        }

        double totalHojeAtual = totalHojeSalvo + litrosPendentesEmMemoria;
        double totalMensalAtual = totalMensalSalvo + litrosPendentesEmMemoria;

        return Map.of(
                "sensorId", sensorId,
                "name", nome,
                "online", online,
                "fluxoAtualLitrosPorMinuto", fluxoAtual,
                "totalFlow", totalHojeAtual,
                "dailyTotal", totalHojeAtual,
                "monthlyTotal", totalMensalAtual,
                "litrosPendentesEmMemoria", litrosPendentesEmMemoria
        );
    }

    private void detectarVazamento(
            SensorEstado estado,
            Device device,
            double fluxoAtual
    ) {
        LocalDateTime agora = LocalDateTime.now();

        if (fluxoAtual < FLUXO_MINIMO_REAL) {
            estado.setInicioFluxoBaixo(null);
            estado.setInicioTorneiraAberta(null);
            estado.setVazamentoAtivo(false);
            return;
        }

        if (fluxoAtual < LIMITE_TORNEIRA_ABERTA) {

            estado.setInicioTorneiraAberta(null);

            if (estado.getInicioFluxoBaixo() == null) {
                estado.setInicioFluxoBaixo(agora);

                System.out.println("=================================");
                System.out.println("FLUXO BAIXO SUSPEITO INICIADO");
                System.out.println("Sensor: " + device.getName());
                System.out.println("Fluxo: " + fluxoAtual + " L/min");
                System.out.println("Regra: 0.10 até 4.99 L/min por 5 minutos");
                System.out.println("=================================");

                return;
            }

            long segundosFluxoBaixo = Duration
                    .between(estado.getInicioFluxoBaixo(), agora)
                    .getSeconds();

            if (segundosFluxoBaixo >= TEMPO_FLUXO_BAIXO_SEGUNDOS) {
                enviarAlertaVazamento(
                        estado,
                        device,
                        fluxoAtual,
                        "Fluxo baixo contínuo por " + segundosFluxoBaixo + " segundos"
                );
            }

            return;
        }

        estado.setInicioFluxoBaixo(null);

        if (estado.getInicioTorneiraAberta() == null) {
            estado.setInicioTorneiraAberta(agora);

            System.out.println("=================================");
            System.out.println("TORNEIRA POSSIVELMENTE ABERTA");
            System.out.println("Sensor: " + device.getName());
            System.out.println("Fluxo: " + fluxoAtual + " L/min");
            System.out.println("Regra: 5 L/min ou mais por 2 minutos");
            System.out.println("=================================");

            return;
        }

        long segundosTorneiraAberta = Duration
                .between(estado.getInicioTorneiraAberta(), agora)
                .getSeconds();

        if (segundosTorneiraAberta >= TEMPO_TORNEIRA_ABERTA_SEGUNDOS) {
            enviarAlertaVazamento(
                    estado,
                    device,
                    fluxoAtual,
                    "Torneira aberta por " + segundosTorneiraAberta + " segundos"
            );
        }
    }

    private void enviarAlertaVazamento(
            SensorEstado estado,
            Device device,
            double fluxoAtual,
            String motivo
    ) {
        LocalDateTime agora = LocalDateTime.now();

        if (estado.getUltimoAlertaVazamento() != null) {
        	long segundosDesdeUltimoAlerta = Duration
        	        .between(
        	            estado.getUltimoAlertaVazamento(),
        	            agora
        	        )
        	        .getSeconds();

        	/*
        	 * 30 SEGUNDOS DE COOLDOWN
        	 */
        	if (segundosDesdeUltimoAlerta < 30) {

        	    System.out.println(
        	        "Alerta bloqueado pelo cooldown de 30 segundos."
        	    );

        	    return;
        	}
        }

        estado.setVazamentoAtivo(true);
        estado.setUltimoAlertaVazamento(agora);

        AlertaVazamento alerta = new AlertaVazamento(
                device.getSensorId(),
                device.getName(),
                device.getOwnerEmail(),
                fluxoAtual
        );

        alertaVazamentoRepository.save(alerta);

        System.out.println("=================================");
        System.out.println("POSSÍVEL VAZAMENTO DETECTADO");
        System.out.println("Sensor ID: " + device.getSensorId());
        System.out.println("Nome: " + device.getName());
        System.out.println("Dono: " + device.getOwnerEmail());
        System.out.println("Fluxo: " + fluxoAtual + " L/min");
        System.out.println("Motivo: " + motivo);
        System.out.println("=================================");

        pushNotificationService.enviarAlertaVazamento(
                device.getOwnerEmail(),
                device.getSensorId(),
                device.getName(),
                fluxoAtual
        );
    }

    private static class SensorEstado {

        private final Long sensorId;

        private String nome;
        private String ownerEmail;

        private Double fluxoAtualLitrosPorMinuto = 0.0;
        private Double litrosPendentesEmMemoria = 0.0;
        private Double dailyTotal = 0.0;
        private Double totalFlow = 0.0;

        private LocalDateTime ultimoSinal;

        private LocalDateTime inicioFluxoBaixo;
        private LocalDateTime inicioTorneiraAberta;
        private LocalDateTime ultimoAlertaVazamento;

        private Boolean vazamentoAtivo = false;

        public SensorEstado(Long sensorId) {
            this.sensorId = sensorId;
            this.nome = "Sensor de Fluxo " + sensorId;
        }

        public Long getSensorId() {
            return sensorId;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getOwnerEmail() {
            return ownerEmail;
        }

        public void setOwnerEmail(String ownerEmail) {
            this.ownerEmail = ownerEmail;
        }

        public Double getFluxoAtualLitrosPorMinuto() {
            return fluxoAtualLitrosPorMinuto;
        }

        public void setFluxoAtualLitrosPorMinuto(Double fluxoAtualLitrosPorMinuto) {
            this.fluxoAtualLitrosPorMinuto = fluxoAtualLitrosPorMinuto;
        }

        public Double getLitrosPendentesEmMemoria() {
            return litrosPendentesEmMemoria;
        }

        public void setLitrosPendentesEmMemoria(Double litrosPendentesEmMemoria) {
            this.litrosPendentesEmMemoria = litrosPendentesEmMemoria;
        }

        public Double getDailyTotal() {
            return dailyTotal;
        }

        public void setDailyTotal(Double dailyTotal) {
            this.dailyTotal = dailyTotal;
        }

        public Double getTotalFlow() {
            return totalFlow;
        }

        public void setTotalFlow(Double totalFlow) {
            this.totalFlow = totalFlow;
        }

        public LocalDateTime getUltimoSinal() {
            return ultimoSinal;
        }

        public void setUltimoSinal(LocalDateTime ultimoSinal) {
            this.ultimoSinal = ultimoSinal;
        }

        public LocalDateTime getInicioFluxoBaixo() {
            return inicioFluxoBaixo;
        }

        public void setInicioFluxoBaixo(LocalDateTime inicioFluxoBaixo) {
            this.inicioFluxoBaixo = inicioFluxoBaixo;
        }

        public LocalDateTime getInicioTorneiraAberta() {
            return inicioTorneiraAberta;
        }

        public void setInicioTorneiraAberta(LocalDateTime inicioTorneiraAberta) {
            this.inicioTorneiraAberta = inicioTorneiraAberta;
        }

        public LocalDateTime getUltimoAlertaVazamento() {
            return ultimoAlertaVazamento;
        }

        public void setUltimoAlertaVazamento(LocalDateTime ultimoAlertaVazamento) {
            this.ultimoAlertaVazamento = ultimoAlertaVazamento;
        }

        public Boolean getVazamentoAtivo() {
            return vazamentoAtivo;
        }

        public void setVazamentoAtivo(Boolean vazamentoAtivo) {
            this.vazamentoAtivo = vazamentoAtivo;
        }
    }
}