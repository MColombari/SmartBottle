import pandas as pd
import MySQLdb
import implicit
from scipy.sparse import csr_matrix

host = 'Yharon.mysql.pythonanywhere-services.com'
user = 'Yharon'
password = 'test_pass'
database = 'Yharon$default'

def get_interactions_hour(hour):
    if hour < 6:
        hour = 6
    elif hour > 19:
        hour = 19

    connection = MySQLdb.connect(host=host, user=user, password=password, database=database)
    cursor = connection.cursor()
    query = ("""
        SELECT user_id, dispenser, COUNT(*) AS interactions
        FROM FILLS
        WHERE HOUR(time) = %s
        GROUP BY user_id, dispenser"""
    )

    cursor.execute(query, (hour,))
    records = cursor.fetchall()
    
    cursor.close()
    connection.close()
    
    columns = ['user_id', 'dispenser', 'interactions']
    df = pd.DataFrame(records, columns=columns)
    return df

    

class Recommender():
    user_dispenser_matrix = None
    model = None

    def __init__(self, hour) -> None:   
        dataset = get_interactions_hour(hour)

        user_item_matrix = pd.pivot_table(dataset, values='interactions', index='user_id', columns='dispenser', fill_value=0)
        self.user_dispenser_matrix  = csr_matrix(user_item_matrix.values)

        self.model = implicit.als.AlternatingLeastSquares(factors=16, regularization=0.01, alpha = 200, iterations=50)
        self.model.fit(self.user_dispenser_matrix)

    def get_recommended_dispensers(self, user_id):
        dispensers = self.model.recommend(user_id-1, self.user_dispenser_matrix[user_id-1], filter_already_liked_items=False, N=5)
        return dispensers
