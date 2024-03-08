import pandas as pd
import MySQLdb
from prophet import Prophet


host = 'Yharon.mysql.pythonanywhere-services.com'
user = 'Yharon'
password = 'test_pass'
database = 'Yharon$default'

THRESHOLD = 1.55

def get_data():
    connection = MySQLdb.connect(host=host, user=user, password=password, database=database)
    cursor = connection.cursor()
    query = ("""
        SELECT dispenser, time
        FROM FILLS"""
    )

    cursor.execute(query)
    records = cursor.fetchall()
    
    cursor.close()
    connection.close()
    
    columns = ['dispenser', 'time']
    df = pd.DataFrame(records, columns=columns)
    return df

    

class Predictor():
    models={}

    def __init__(self) -> None:   
        dataset = get_data()
        dataset['ds'] = dataset['time'].dt.floor('H')
        ids = dataset.dispenser.unique()
        data_grouped = dataset.groupby(['ds', 'dispenser']).size().reset_index(name='y')

        start_date = pd.to_datetime('2023-01-01 00:00:00')
        end_date = pd.to_datetime('2023-12-31 23:00:00')
        complete_datetime_index = pd.date_range(start=start_date, end=end_date, freq='H')
        all_df = pd.DataFrame({'ds': complete_datetime_index})

        for i in ids:
            data = data_grouped[data_grouped.dispenser == i]
            complete_df = pd.merge(all_df, data, on='ds', how='left')
            complete_df['y'] = complete_df['y'].fillna(0)
            model = Prophet()
            model.fit(complete_df)
            self.models[i]=model


    def is_busy(self, dispenser, time):
        rounded_datetime = time.replace(minute=0, second=0, microsecond=0)
        future_data = pd.DataFrame({'ds': [rounded_datetime]})
        forecast = self.models[dispenser].predict(future_data)
        return forecast['yhat'].values[0] > THRESHOLD
