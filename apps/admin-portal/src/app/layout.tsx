/*
 * Purpose: Defines the minimal admin portal document shell.
 * Why it exists: Allows the admin frontend service to build and run with a health surface before workflows exist.
 * Architecture fit: Anchors the approved administration and governance application boundary.
 */
import type { Metadata } from 'next';
import type { ReactNode } from 'react';

export const metadata: Metadata = {
  title: 'AI Rural Admin Portal',
  description:
    'Sprint 1 administration overview for platform services and production-readiness gates.',
};

export default function RootLayout({ children }: Readonly<{ children: ReactNode }>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
