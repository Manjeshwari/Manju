from apscheduler.schedulers.blocking import BlockingScheduler

from operations import Operations

sched = BlockingScheduler()
ops = Operations()


@sched.scheduled_job('interval', seconds=15)
def update_15_seconds():
    print('Update device locations every 15 seconds.')
    ops.update_device_locale()


sched.start()
