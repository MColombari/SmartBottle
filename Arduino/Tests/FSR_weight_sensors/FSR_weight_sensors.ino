#define IN_PIN_0 A0
#define IN_PIN_1 A1
#define IN_PIN_2 A2
#define IN_PIN_3 A3
#define FILTER_DIMENSION 200
#define PRINT_PERIOD 1000

/*
  Description: for the breadbord test i chose for each resistor a 10K pull-up resistor.
*/

unsigned long start_time;

int values_0[FILTER_DIMENSION];    // Digital filter 0.
int values_1[FILTER_DIMENSION];    // Digital filter 1.
int values_2[FILTER_DIMENSION];    // Digital filter 2.
int values_3[FILTER_DIMENSION];    // Digital filter 3.

int index;

int means(int in[FILTER_DIMENSION]){
  double ret = 0;
  for(int i = 0; i < FILTER_DIMENSION; i++){
    ret += in[i];
  }
  ret /= FILTER_DIMENSION;
  return ret;
}

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  start_time = millis();

  index = 0;

  for (int i = 0; i < FILTER_DIMENSION; i++){
    values_0[i] = 0;
    values_1[i] = 0;
    values_2[i] = 0;
    values_3[i] = 0;
  }
}

void loop() {
  if(index >= FILTER_DIMENSION){
    index = 0;
  }
  values_0[index] = analogRead(IN_PIN_0);
  values_1[index] = analogRead(IN_PIN_1);
  values_2[index] = analogRead(IN_PIN_2);
  values_3[index] = analogRead(IN_PIN_3);
  index++;
  
  if(PRINT_PERIOD <= millis() - start_time){
    int tot_means[4];
    tot_means[0] = means(values_0);
    tot_means[1] = means(values_1);
    tot_means[2] = means(values_2);
    tot_means[3] = means(values_3);
    int tot = tot_means[0] + tot_means[1] + tot_means[2] + tot_means[3];
    tot /= 4;
    Serial.print(map(tot, 1024, 0, 0, 100)); Serial.print(",");
    Serial.print(tot_means[0]); Serial.print(",");
    Serial.print(tot_means[1]); Serial.print(",");
    Serial.print(tot_means[2]); Serial.print(",");
    Serial.println(tot_means[3]);
    start_time = millis();
  }
}
