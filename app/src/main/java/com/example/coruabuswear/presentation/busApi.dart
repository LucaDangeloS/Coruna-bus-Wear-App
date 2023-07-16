import 'dart:developer';
import '../models/bus.dart';
import 'package:http/http.dart' as http;
import '../constants.dart' show ApiURLs;

class APIException implements Exception {
  String cause;
  APIException(this.cause);
}

class BusApi {
  static Future<List<Bus>> getStopsNearby(
      double lat, double lng, int radius, int n) async {
    // URL + lat + lng + radio + n_paradas
    Uri uri = Uri.parse(
        '${ApiURLs.baseBusURL}&dato=${lat}_${lng}_${radius}_$n&func=3');

    try {
      // Make get request
      var response = await http.get(uri);

      if (response.statusCode == 200) {
        List<Bus> buses = [];
        // TODO
        log(response.body);
        return buses;
      } else {
        throw APIException('Failed to load bus stops');
      }
    } catch (e) {
      throw APIException('Failed to load bus stops');
    }
  }
}
