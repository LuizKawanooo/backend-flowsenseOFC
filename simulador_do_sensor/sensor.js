const mqtt = require('mqtt');

const client = mqtt.connect('mqtt://localhost:1883');

const sensores = [
  {
    sensorId: 1,
    nome: 'Banheiro',
    fluxoMin: 3,
    fluxoMax: 7
  },
  {
    sensorId: 2,
    nome: 'Cozinha',
    fluxoMin: 4,
    fluxoMax: 9
  },
  {
    sensorId: 3,
    nome: 'Área Externa',
    fluxoMin: 2,
    fluxoMax: 6
  }
];

client.on('connect', () => {
  console.log('Sensores virtuais conectados ao MQTT');

  setInterval(() => {
    sensores.forEach(sensor => {
      const fluxo = gerarFluxo(sensor.fluxoMin, sensor.fluxoMax);

      const payload = JSON.stringify({
        sensorId: sensor.sensorId,
        fluxo: fluxo
      });

      client.publish('sensor/fluxo', payload);

      console.log(`Sensor ${sensor.sensorId} - ${sensor.nome}:`, payload);
    });
  }, 1000);
});

function gerarFluxo(min, max) {
  const valor = min + Math.random() * (max - min);
  return Number(valor.toFixed(2));
}