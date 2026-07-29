/*
 * Purpose: Defines the minimal web portal document shell.
 * Why it exists: Allows the frontend service to build and run with a health surface before product UI exists.
 * Architecture fit: Anchors the approved user-facing Next.js application boundary.
 */
import type { Metadata } from 'next';
import type { ReactNode } from 'react';

export const metadata: Metadata = {
  title: 'AI Rural Root Cause Platform',
  description: 'Operational shell for the user-facing web portal.',
};

export default function RootLayout({ children }: Readonly<{ children: ReactNode }>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
