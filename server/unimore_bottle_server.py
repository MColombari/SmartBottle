import MySQLdb
from flask import Flask, jsonify, request
from telegram import Bot, Update
from telegram.ext import ApplicationBuilder, CommandHandler, ContextTypes
import asyncio
from datetime import datetime
import dispenser_suggestions as ds
import engagement_predictor as ep


url = 'https://yharon.pythonanywhere.com'
host = 'Yharon.mysql.pythonanywhere-services.com'
user = 'Yharon'
password = 'test_pass'
database = 'Yharon$default'


bot_token = "6393895159:AAFKBko-D908Ed3sW2z71RWeOQHFQtYctr0"
bot = None

loop = asyncio.get_event_loop()
app = Flask(__name__)

ERROR_MARGIN = 3



def daily_levels(bottle):
    query = """
            select value
            from READINGS
            where bottle_id = %s
                and DATE(date_time) = CURDATE()
            """
    result_list = []
    try:
        connection = MySQLdb.connect(host=host, user=user, password=password, database=database)
        cursor = connection.cursor()
        cursor.execute(query, (bottle,))

        result_list = [row[0] for row in cursor.fetchall()]
    except:
        print('Database unreachable')
    finally:
        if connection:
            connection.close()

    return result_list


def get_capacity(bottle):
    query = """
            select capacity
            from BOTTLES
            where bottle_id = %s
            """
    result = 0

    try:
        connection = MySQLdb.connect(host=host, user=user, password=password, database=database)
        cursor = connection.cursor()
        cursor.execute(query, (bottle,))

        result = cursor.fetchone()[0]
    except:
        print('Database unreachable')
    finally:
        if connection:
            connection.close()

    return result


def water_drank_bottle(bottle):
    water_drank = 0
    avg_count = 0
    for value in daily_levels(bottle):
        if avg_count == 0:
            avg = value
            avg_count = 1
        else:
            if abs(avg-value) <= ERROR_MARGIN:
                avg = (avg * avg_count + value) / (avg_count + 1)
                avg_count += 1
            else:
                if value > avg:
                    avg = value
                    avg_count = 1
                else:
                    water_drank += avg - value
                    avg = value
                    avg_count = 1

    return(water_drank * get_capacity(bottle) / 100)

@app.route('/water_drank/<int:user_id>', methods=['GET'])
def water_drank_user(user_id):
    query = """
            select bottle_id
            from BOTTLES
            where user_id = %s
            """
    water_drank = 0
    try:
        connection = MySQLdb.connect(host=host, user=user, password=password, database=database)
        cursor = connection.cursor()
        cursor.execute(query, (user_id,))
        for row in cursor.fetchall():
            water_drank += water_drank_bottle(row[0])

        return str(water_drank)

    except:
        return '0'
    finally:
        if connection:
            connection.close()


@app.route('/readings', methods=['POST'])
def add_reading():
    data = request.json

    # Validate that the 'readings' field is present and is a list
    if 'readings' not in data or not isinstance(data['readings'], list):
        return jsonify({'error': 'Invalid data format. The "readings" field must be a list.'}), 400

    try:
        connection = MySQLdb.connect(host=host, user=user, password=password, database=database)
        for reading in data['readings']:
            bottle_id = reading.get('bottle_id')
            date_time = reading.get('date_time')
            value = reading.get('value')

            # Validate that required data is provided for each reading
            if bottle_id is None or date_time is None or value is None:
                return jsonify({'error': 'Invalid data in batch. Make sure each reading includes bottle_id, date_time, and value.'}), 401

            with connection.cursor() as cursor:
                    cursor.execute("INSERT INTO READINGS (bottle_id, date_time, value) VALUES (%s, %s, %s)",
                                   (bottle_id, date_time, value))
            
            connection.commit()

        return jsonify({'message': 'Readings added successfully'}), 201
    except:
        return jsonify({'error': 'Readings could not be added'}), 402
    finally:
        if connection:
            connection.close()


@app.route('/fills', methods=['POST'])
def add_fill():
    data = request.json

    # Validate that required fields are present
    if 'bottle_id' not in data or 'dispenser_id' not in data or 'time' not in data:
        return jsonify({'error': 'Invalid data format. Make sure "bottle_id", "dispenser_id", and "time" are provided.'}), 400

    bottle_id = data['bottle_id']
    dispenser_id = data['dispenser_id']
    time = data['time']

    try:
        connection = MySQLdb.connect(host=host, user=user, password=password, database=database)
        
        # Retrieve the user_id from the BOTTLES table based on the provided bottle_id
        with connection.cursor() as cursor:
            cursor.execute("SELECT user_id FROM BOTTLES WHERE bottle_id = %s", (bottle_id,))
            result = cursor.fetchone()

            if result is None:
                return jsonify({'error': f'Bottle with id {bottle_id} not found.'}), 404

            user_id = result[0]

            # Validate that the dispenser_id exists
            cursor.execute("SELECT * FROM DISPENSERS WHERE id = %s", (dispenser_id,))
            if cursor.fetchone() is None:
                return jsonify({'error': f'Dispenser with id {dispenser_id} not found.'}), 404

        # Insert the fill into the FILLS table
        with connection.cursor() as cursor:
            cursor.execute("INSERT INTO FILLS (user_id, dispenser, time) VALUES (%s, %s, %s)",
                           (user_id, dispenser_id, time))

        connection.commit()

        return jsonify({'message': 'Fill added successfully'}), 201
    except Exception as e:
        return jsonify({'error': f'Fill could not be added. {str(e)}'}), 402
    finally:
        if connection:
            connection.close()
            

def get_subscribed_chat_ids():
    query = """
            select chat_id
            from SUBSCRIBED_CHAT_IDS
            """
    result_list = []
    try:
        connection = MySQLdb.connect(host=host, user=user, password=password, database=database)
        cursor = connection.cursor()
        cursor.execute(query)

        result_list = [row[0] for row in cursor.fetchall()]
    except:
        print('Database unreachable')
    finally:
        if connection:
            connection.close()
    return result_list


async def notify_staff(text):
    await asyncio.gather(*[Bot(bot_token).send_message(chat_id=user, text=text) for user in get_subscribed_chat_ids()])


@app.route('/notifications', methods=['POST'])
def add_notification_http():
    try:
        data = request.get_json()

        if 'dispenser_id' not in data or not data['dispenser_id']:
            return jsonify({'error': 'A dispenser ID must be provided.'}), 400

        dispenser_id = int(data['dispenser_id'])
        description = data.get('description', None)

        connection = MySQLdb.connect(host=host, user=user, password=password, database=database)
        cursor = connection.cursor()

        n = cursor.execute("""
                           SELECT * FROM DISPENSERS
                           WHERE id = %s""",
                           (dispenser_id,))

        if n == 0:
            return jsonify({'error': f'Dispenser with ID {dispenser_id} does not exist. Please provide a valid dispenser ID.'}), 400

        _, name, location = cursor.fetchone()

        # Inserting a new notification into the database
        cursor.execute("""
                       INSERT INTO NOTIFICATIONS (dispenser_id, description)
                       VALUES (%s, %s)""",
                       (dispenser_id, description))

        # Commit the changes to the database
        connection.commit()

        message = f'New notification\nDispenser: {dispenser_id} ({name}, {location})'
        if description is not None:
            message += f'\nDescription: {description}'

        loop.run_until_complete(notify_staff(message))

        return jsonify({'message': 'Notification added successfully.'}), 200

    except Exception as e:
        return jsonify({'error': str(e)}), 500
    finally:
        if connection:
            connection.close()


@app.route('/debug/notify')
def d_list():
    loop.run_until_complete(Bot(bot_token).send_message(chat_id=2061627716, text='personal test'))
    return "finished"




current_recs = {'time' : None, 'model' : None}

@app.route('/recommendations/<int:user_id>', methods=['GET'])
def get_recommendations(user_id):
    try:
        now = datetime.now()
        if current_recs['time'] is None or \
            now.year != current_recs['time'].year or \
            now.month != current_recs['time'].month or \
            now.day != current_recs['time'].day or \
            now.hour != current_recs['time'].hour:
            
            current_recs['time'] = now
            current_recs['model'] = ds.Recommender(now.hour)

        recs, _ = current_recs['model'].get_recommended_dispensers(user_id)
        dispensers = []

        for id in recs:
            query = """
                select name, location
                from DISPENSERS
                where id = %s
                """

            connection = MySQLdb.connect(host=host, user=user, password=password, database=database)
            cursor = connection.cursor()
            cursor.execute(query, (id,))
            result = cursor.fetchone()
            if result:
                dispensers.append({'id' : str(id), 'name' : result[0], 'location' : result[1]})

        return jsonify({'dispensers': dispensers})
    except Exception as e:
        return {'Error' : str(e)}


predictor = None

@app.route('/engagement/train', methods=['GET'])
def engagement_train():
    try:
        global predictor
        predictor = ep.Predictor()
    except Exception as e:
        return {'Error' : str(e)}


@app.route('/engagement/is_busy/<int:dispenser>', methods=['GET'])
def is_busy(dispenser):
    if predictor is None:
        engagement_train()
    now = datetime.now()
    return str(predictor.is_busy(dispenser, now))

@app.route('/bottles/register', methods=['POST'])
def register_bottle():
    data = request.json

    # Validate that required fields are present
    if 'bottle_id' not in data or 'user_id' not in data or 'capacity' not in data:
        return jsonify({'error': 'Invalid data format. Make sure "bottle_id", "user_id", and "capacity" are provided.'}), 400

    bottle_id = data['bottle_id']
    user_id = data['user_id']
    capacity = data['capacity']

    try:
        connection = MySQLdb.connect(host=host, user=user, password=password, database=database)

        # Check if the bottle is already inserted
        with connection.cursor() as cursor:
            cursor.execute("SELECT * FROM BOTTLES WHERE bottle_id = %s", (bottle_id,))
            existing_bottle = cursor.fetchone()

            if existing_bottle is not None:
                return jsonify({'message': f'Bottle with id {bottle_id} is already registered for user {existing_bottle["user_id"]}.'}), 200

            # Insert the bottle into the BOTTLES table
            cursor.execute("INSERT INTO BOTTLES (bottle_id, user_id, capacity) VALUES (%s, %s, %s)",
                           (bottle_id, user_id, capacity))

        connection.commit()

        return jsonify({'message': 'Bottle registered successfully'}), 201
    except Exception as e:
        return jsonify({'error': f'Bottle could not be registered. {str(e)}'}), 402
    finally:
        if connection:
            connection.close()

@app.route('/bottles/remove/<int:bottle_id>', methods=['GET'])
def delete_bottle(bottle_id):
    try:
        connection = MySQLdb.connect(host=host, user=user, password=password, database=database)
        cursor = connection.cursor()

        # Check if the bottle_id exists in the database
        cursor.execute("SELECT * FROM BOTTLES WHERE bottle_id = %s", (bottle_id,))
        bottle = cursor.fetchone()

        if bottle:
            cursor.execute("DELETE FROM BOTTLES WHERE bottle_id = %s", (bottle_id,))
            connection.commit()
            return jsonify({'success': True, 'message': f'Bottle with id {bottle_id} deleted successfully'})
        else:
            return jsonify({'success': False, 'error': f'Bottle with id {bottle_id} not found'}), 404
    except Exception as e:
        return jsonify({'success': False, 'error': str(e)}), 500
    finally:
        if connection:
            connection.close()


application = ApplicationBuilder().token(bot_token).build()

async def start(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    try:
        connection = MySQLdb.connect(host=host, user=user, password=password, database=database)
        cursor = connection.cursor()
        cursor.execute("""
                           insert ignore into SUBSCRIBED_CHAT_IDS VALUES(%s)
                           """, (update.message.chat_id, ))


        connection.commit()

        await update.message.reply_text('You will now receive dispenser notifications')
    except:
        await update.message.reply_text('Failed to start. Please try again later.')
    finally:
        if connection:
            connection.close()

async def stop(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    try:
        connection = MySQLdb.connect(host=host, user=user, password=password, database=database)
        cursor = connection.cursor()
        cursor.execute("""
                            delete from SUBSCRIBED_CHAT_IDS
                            where chat_id = %s
                            """, (update.message.chat_id, ))

        connection.commit()

        await update.message.reply_text('You will not receive dispenser notifications anymore')
    except:
        await update.message.reply_text('Failed to stop. Please try again later.')
    finally:
        if connection:
            connection.close()

async def list_notifications(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    try:
        connection = MySQLdb.connect(host=host, user=user, password=password, database=database)
        cursor = connection.cursor()

        n = cursor.execute("""
                           SELECT notification_id, dispenser_id, name, location, description
                           FROM NOTIFICATIONS join DISPENSERS on NOTIFICATIONS.dispenser_id = DISPENSERS.id""")
        notifications = cursor.fetchall()

        if n == 0:
            await update.message.reply_text('There are no notifications.')
        else:
            await update.message.reply_text('Current notifications')
            for i in notifications:
                notification_id, dispenser_id, name, location, description = i
                message = f'Notification ID: {notification_id}\nDispenser: {dispenser_id} ({name}, {location})'
                if description is not None:
                    message += f'\nDescription: {description}'
                await update.message.reply_text(message)

    except Exception:
        await update.message.reply_text('Notifications could not be fetched')
    finally:
        if connection:
            connection.close()

async def add_notification(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    try:
        if context.args is None or len(context.args) < 1:
            await update.message.reply_text('A dispenser ID must be provided.')
            return

        dispenser_id = int(context.args[0])
        description = ' '.join(context.args[1:]) if len(context.args) > 1 else None

        connection = MySQLdb.connect(host=host, user=user, password=password, database=database)
        cursor = connection.cursor()

        n = cursor.execute("""
                           SELECT * FROM DISPENSERS
                           WHERE id = %s""",
                           (dispenser_id,))

        if n == 0:
            await update.message.reply_text(f'Dispenser with ID {dispenser_id} does not exist. Please provide a valid dispenser ID.')
            return

        _, name, location = cursor.fetchone()

        # Inserting a new notification into the database
        cursor.execute("""
                       INSERT INTO NOTIFICATIONS (dispenser_id, description)
                       VALUES (%s, %s)""",
                       (dispenser_id, description))

        # Commit the changes to the database
        connection.commit()

        message = f'New notification\nDispenser: {dispenser_id} ({name}, {location})'
        if description is not None:
            message += f'\nDescription: {description}'
        await notify_staff(message)

    except Exception:
        await update.message.reply_text('Failed to add notification. Please try again later.')
    finally:
        if connection:
            connection.close()

async def remove_notification(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    try:
        if context.args is None or len(context.args) < 1:
            await update.message.reply_text('Please provide the notification ID.')
            return

        notification_id = int(context.args[0])

        connection = MySQLdb.connect(host=host, user=user, password=password, database=database)
        cursor = connection.cursor()

        # Check if the notification exists
        n = cursor.execute("""
                           SELECT * FROM NOTIFICATIONS
                           WHERE notification_id = %s""",
                           (notification_id,))

        if n == 0:
            await update.message.reply_text('Notification not found.')
            return

        # Delete the notification from the database
        cursor.execute("""
                       DELETE FROM NOTIFICATIONS
                       WHERE notification_id = %s""",
                       (notification_id,))

        # Commit the changes to the database
        connection.commit()

        await update.message.reply_text('Notification removed successfully!')

    except Exception:
        await update.message.reply_text('Failed to remove notification. Please try again later.')
    finally:
        if connection:
            connection.close()



application.add_handler(CommandHandler("start", start))
application.add_handler(CommandHandler("stop", stop))
application.add_handler(CommandHandler("list", list_notifications))
application.add_handler(CommandHandler("add_notification", add_notification))
application.add_handler(CommandHandler("remove_notification", remove_notification))

@app.route('/telegram', methods=['POST'])
async def webhook():
    if request.headers.get('content-type') == 'application/json':
        async with application:
            update = Update.de_json(request.get_json(force=True),application.bot)
            await application.process_update(update)
            return ('', 204)
    else:
        return ('Bad request', 400)
