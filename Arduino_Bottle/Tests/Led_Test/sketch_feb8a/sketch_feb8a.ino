#include <FastLED.h>

#define LED_NUM 24
#define LED_PIN 11

CRGB leds[1][LED_NUM];

void setup() {
  Serial.begin(9600);

  FastLED.addLeds<WS2812B, LED_PIN, GBR>(leds[0], LED_NUM);
  FastLED.setBrightness(100);
}

void loop(){
  FastLED.setBrightness(100);

  for(int i = 0;  i < LED_NUM; i++){
    leds[0][i] = CRGB(255, 0, 0);
  }
  FastLED.show();

  delay(1000);

  for(int i = 0;  i < LED_NUM; i++){
    leds[0][i] = CRGB(0, 255, 0);
  }
  FastLED.show();

  delay(1000);

  for(int i = 0;  i < LED_NUM; i++){
    leds[0][i] = CRGB(0, 0, 255);
  }
  FastLED.show();

  delay(100);
}