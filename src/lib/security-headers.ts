import { secureHeaders } from "hono/secure-headers";
import type { MiddlewareHandler } from "hono";
import { isHttps } from "./auth";

export const baseSecurityHeaders = secureHeaders({
  contentSecurityPolicy: {
    defaultSrc: ["'none'"],
    baseUri: ["'none'"],
    formAction: ["'self'"],
    frameAncestors: ["'none'"],
    imgSrc: ["'self'", "data:", "https:"],
    styleSrc: ["'self'"],
    fontSrc: ["'self'"],
    scriptSrc: ["'none'"],
    connectSrc: ["'self'"],
    objectSrc: ["'none'"],
    manifestSrc: ["'self'"],
  },
  referrerPolicy: "no-referrer",
  crossOriginOpenerPolicy: "same-origin",
  crossOriginResourcePolicy: "same-origin",
  originAgentCluster: "?1",
  xContentTypeOptions: "nosniff",
  xFrameOptions: "DENY",
  xXssProtection: "0",
  strictTransportSecurity: false,
  permissionsPolicy: {
    accelerometer: [],
    camera: [],
    geolocation: [],
    gyroscope: [],
    magnetometer: [],
    microphone: [],
    payment: [],
    usb: [],
  },
});

export const noStoreAndRobots: MiddlewareHandler = async (c, next) => {
  await next();
  c.header("X-Robots-Tag", "noindex, nofollow, noarchive, nosnippet");
  c.header("Cache-Control", "private, no-store, max-age=0, must-revalidate");
  c.header("Pragma", "no-cache");
  if (isHttps(c.req.raw)) {
    c.header(
      "Strict-Transport-Security",
      "max-age=31536000; includeSubDomains; preload",
    );
  }
};
