import cProfile
import json
from collections import Counter, OrderedDict

import numpy as np


class Localize:
    def __init__(self):
        self.location_data = {}
        self._user_data = {}
        self._prepped_data = {}
        self._locations = {}
        self._results = {}

    def process(self):
        """
        Preprocess the location data.
        """
        def compute_average():
            """
            Compute the average of each modems signal across all sample scans.
            :rtype: Dict{Str:Int}
            """
            averages = {}
            for locale in self.location_data:
                # Get the union set of modem IDs (BSSIDs) for a locale.
                bssids = []
                for sample in self.location_data[locale]:
                    bssids.append(
                        self.location_data[locale][sample]["data"].keys())
                bssids = set().union(*bssids)
                # For each modem ID in a sample count it's occurence and cumulate the sum its value.
                for sample in self.location_data[locale]:
                    for bssid in bssids:
                        # Modem ID exists in both location data and user data.
                        if bssid in self.location_data[locale][sample]["data"]:
                            averages.setdefault(locale, {})
                            averages[locale].setdefault(
                                bssid, {"sum": 0, "count": 0})
                            averages[locale][bssid]["sum"] += self.location_data[locale][sample]["data"][bssid]
                            averages[locale][bssid]["count"] += 1
            # Compute the average of each modem ID for a lcoation.
            for locale in averages:
                for bssid in averages[locale]:
                    averages[locale][bssid] = averages[locale][bssid]["sum"] / \
                        averages[locale][bssid]["count"]
            return averages

        self._prepped_data = compute_average()

    def filter(self):
        """
        Filter modem IDs that do not exists in either location or user data.
        """
        if self._user_data is None or self._user_data == {} or type(self._user_data) != dict:
            self._prepped_data == {}
            return

        locations = {}
        user_data = self._user_data["data"]
        # Get the set of modem IDs that exists in both the location data and user data.
        for locale in self._prepped_data:
            locations.setdefault(locale, set())
            location_data = self._prepped_data[locale]
            locations[locale] = set(location_data) & set(user_data)
        # Get locations that have no modem IDs.
        locations_to_delete = set()
        for locale in locations.keys():
            if len(locations[locale]) == 0:
                # Perform Union Operation:
                locations_to_delete |= set(locale)
        # Remove locations that have no modem IDs.
        for locale in locations_to_delete:
            locations.pop(locale, None)
        self._locations = locations

    def calculate(self):
        """
        Perform calculations to determine the error rate of the user data regarding locations.
        """
        if self._user_data is None or self._user_data == {} or type(self._user_data) != dict:
            self._results = {}
            return

        results = {}
        user_data = self._user_data["data"]
        # Calculate the positions where a and b differ using yt != yp, then find the mean of those values:
        for locale in self._locations:
            location_data = self._prepped_data[locale]
            key_set = set(location_data) & set(user_data)
            yt = np.array(list([location_data[k]
                                for k in key_set if k in location_data]))
            yp = np.array(list([user_data[k]
                                for k in key_set if k in user_data]))
            results[locale] = np.mean(yt != yp)
        self._results = results

    def location(self):
        """
        Return the location with the lowest error rate.
        :rtype: Dict{Str: Str}
        """
        result = ""
        value = None
        for locale in self._results:
            if value is None:
                result = locale
                value = self._results[locale]
            if value > self._results[locale]:
                result = locale
                value = self._results[locale]
        return {"locale": result}

    def locate(self):
        self.calculate()
        return self.location()

    def get_data(self):
        return self.location_data

    def set_data(self, data):
        self.location_data = data
        self.process()

    def get_user(self):
        return self._user_data

    def set_user(self, data):
        self._user_data = data
        self.filter()


def main():
    with open("data.json", "r") as f:
        locations = json.loads(f.read())
    with open("test.json", "r") as f:
        sample = json.loads(f.read())

    localize = Localize()
    localize.set_data(locations)
    for data in sample:
        localize.set_user(sample[data])
        print({"Actual": data, "Predicted": localize.locate()["locale"]})
    localize.set_user({})
    print({"Actual": data, "Predicted": localize.locate()["locale"]})
    print()


if __name__ == "__main__":
    cProfile.run("main()")
