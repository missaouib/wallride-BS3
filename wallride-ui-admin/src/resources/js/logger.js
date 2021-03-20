// wallride-ui-admin/src/resources/js/logger.js
const winston = require('winston');
require('winston-socket.io');

export const logger = winston.createLogger({
　level: 'info',
　// Use winston.format.splat() to enable string interpolation using %s in the log() method
　format: winston.format.combine(
　　winston.format.splat(),
　　winston.format.simple()
　),
　defaultMeta: { service: 'wallride-ui-admin' },
　transports: [
　　new winston.transports.Console(),

　　// The default winston-socket.io host is http://localhost, and the default port is 3000
　　new winston.transports.SocketIO({
　　　host: "http://localhost",
　　　secure: false,
　　　reconnect: false,
　　　namespace: 'log',
　　　log_topic: 'log'
　　})
　]
}); 
