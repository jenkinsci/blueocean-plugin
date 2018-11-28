import { createRenderExtensionPoint, createExtensionPoint } from '@jenkins-cd/es-extensions';

export const TopLinkExtensionPoint = createRenderExtensionPoint<{ navigate: Function }>("jenkins.blueocean.top.links")
export const AdminLinkExtensionPoint = createRenderExtensionPoint<{ navigate: Function}>("jenkins.blueocean.top.admin")
export const RouteExtensionPoint = createExtensionPoint<{ path: string, render: Function}>("jenkins.blueocean.routes")
export const LogoExtensionPoint = createRenderExtensionPoint("jenkins.header.logo")