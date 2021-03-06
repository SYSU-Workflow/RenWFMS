#!/usr/bin/env python
# encoding: utf-8

"""
@module : WebUILogModel
@author : Rinkako
@time   : 2018/5/31
"""
import time


class WebUILogModel:
    """
    Model Class: Data model operation for runtime logging of Ren Web UI.
    """

    def __init__(self):
        pass

    @staticmethod
    def Initialize(forced=False):
        """
        Initialize the logger.
        :param forced: forced reinitialize
        """
        if forced is False and WebUILogModel._persistDAO is not None:
            return
        from DAO import MySQLDAO
        WebUILogModel._persistDAO = MySQLDAO.MySQLDAO()
        WebUILogModel._persistDAO.Initialize()

    @staticmethod
    def Dispose():
        """
        Dispose the resource of runtime logger.
        """
        if WebUILogModel._persistDAO is not None:
            WebUILogModel._persistDAO.Dispose()
        WebUILogModel._persistDAO = None

    @staticmethod
    def LogToSteady(label, level, message, timestamp, dp=0):
        """
        Write log to the runtime steady logging data model.
        :param label: event label
        :param level: event level
        :param message: event description
        :param timestamp: event timestamp
        :param dp: depth of exception stack
        """
        t = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(timestamp))
        sql = "INSERT INTO ren_webuilog(label, level, message, timestamp) VALUES ('%s', '%s', '%s', '%s')" \
              % (label, level, message, t)
        if WebUILogModel._persistDAO is None:
            WebUILogModel.Initialize()
        WebUILogModel._persistDAO.ExecuteSQL(sql, False, dp, updateVersion=False)

    @staticmethod
    def LogError(label, message, timestamp=None):
        """
        Quick log an error event.
        :param label: event label
        :param message: event description
        :param timestamp: event timestamp, current time if None
        """
        if timestamp is None:
            timestamp = time.time()
        WebUILogModel.LogToSteady(label, "Error", message, timestamp)

    @staticmethod
    def LogInformation(label, message, timestamp=None):
        """
        Quick log information event.
        :param label: event label
        :param message: event description
        :param timestamp: event timestamp, current time if None
        """
        if timestamp is None:
            timestamp = time.time()
        WebUILogModel.LogToSteady(label, "Info", message, timestamp)

    @staticmethod
    def LogUnauthorized(label, message, timestamp=None):
        """
        Quick log an unauthorized event.
        :param label: event label
        :param message: event description
        :param timestamp: event timestamp, current time if None
        """
        if timestamp is None:
            timestamp = time.time()
        WebUILogModel.LogToSteady(label, "Unauthorized", message, timestamp)

    """
    Persist DAO
    """
    _persistDAO = None
