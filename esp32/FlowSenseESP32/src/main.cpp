#include <Arduino.h>
#include <WiFi.h>
#include <WebServer.h>
#include <Preferences.h>
#include <PubSubClient.h>

#define FLOW_PIN 27

volatile unsigned long pulseCount = 0;

void IRAM_ATTR countPulse() {
  pulseCount++;
}

void setup() {
  Serial.begin(115200);

  pinMode(FLOW_PIN, INPUT);

  attachInterrupt(digitalPinToInterrupt(FLOW_PIN), countPulse, RISING);

  Serial.println("ESP32 Flow-Sense iniciado no PlatformIO");
}

void loop() {
  static unsigned long lastTime = 0;

  if (millis() - lastTime >= 1000) {
    noInterrupts();
    unsigned long pulses = pulseCount;
    pulseCount = 0;
    interrupts();

    float frequency = pulses;
    float fluxoLitrosMinuto = frequency / 7.5;

    Serial.print("Pulsos: ");
    Serial.print(pulses);

    Serial.print(" | Fluxo: ");
    Serial.print(fluxoLitrosMinuto);
    Serial.println(" L/min");

    lastTime = millis();
  }
}