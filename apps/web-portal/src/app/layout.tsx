/*
 * Purpose: Defines the minimal web portal document shell.
 * Why it exists: Allows the frontend service to build and run with a health surface before product UI exists.
 * Architecture fit: Anchors the approved user-facing Next.js application boundary.
 */
import type { Metadata } from 'next';
import type { ReactNode } from 'react';
import './globals.css';

export const metadata: Metadata = {
  title: 'Enterprise Rural Intelligence Platform',
  description: 'Sprint 1 MVP for survey, evidence, AI analysis, RAG, recommendations, and reports.',
};

export default function RootLayout({ children }: Readonly<{ children: ReactNode }>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
