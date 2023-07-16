import 'package:flutter/material.dart';

class BusLine {
  String name;
  Color color;

  BusLine(this.name, this.color);

  BusLine parseFromJson(Map<String, dynamic> json) {
    return BusLine(json['nombre'], Colors.black);
  }
}

class Bus {
  BusLine line;
  int id;
  int remainingTime;

  Bus(this.line, this.id, this.remainingTime);

  void updateRemainingTime(int newRemainingTime) {
    remainingTime = newRemainingTime;
  }

  Bus parseFromJson(Map<String, dynamic> json) {
    return Bus(
        BusLine(json['linea'], Colors.black), json['id'], json['tiempo']);
  }
}

class BusStop {
  String? name;
  int code;
  int distance;
  List<Bus> buses = [];

  BusStop.withName(this.name, this.code, this.distance);
  BusStop.withBuses(this.name, this.code, this.distance, this.buses);
  BusStop(this.code, this.distance);

  BusStop parseFromJson(Map<String, dynamic> json) {
    return BusStop.withName(json['nombre'], json['codigo'], json['distancia']);
  }
}
