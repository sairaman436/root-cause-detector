// Purpose: Defines baseline Next.js runtime configuration for the admin portal.
// Why it exists: Provides a production-aware administration frontend entry point before admin workflows are implemented.
// Architecture fit: Supports the approved separated governance and operations portal boundary.
const nextConfig = {
  output: 'standalone',
  reactStrictMode: true,
  poweredByHeader: false,
};

export default nextConfig;
