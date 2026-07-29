// Purpose: Defines baseline Next.js runtime configuration for the web portal.
// Why it exists: Provides a production-aware frontend configuration entry point before UI implementation.
// Architecture fit: Supports the approved React/TypeScript frontend app boundary.
const nextConfig = {
  output: 'standalone',
  reactStrictMode: true,
  poweredByHeader: false,
};

export default nextConfig;
