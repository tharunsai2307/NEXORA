import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { Role } from '../../types/auth';
import { ShieldX } from 'lucide-react';
import { Link } from 'react-router-dom';

interface RoleGuardProps {
  allowedRoles: Role[];
  children: React.ReactNode;
}

export const RoleGuard: React.FC<RoleGuardProps> = ({ allowedRoles, children }) => {
  const { user, hasRole } = useAuth();

  if (!user || !hasRole(allowedRoles)) {
    return (
      <div className="p-8 max-w-2xl mx-auto my-12 bg-slate-900/90 border border-red-500/30 rounded-xl shadow-2xl backdrop-blur-md text-center">
        <div className="w-14 h-14 mx-auto rounded-full bg-red-500/10 border border-red-500/30 flex items-center justify-center mb-4 text-red-400">
          <ShieldX className="w-8 h-8" />
        </div>
        <h2 className="text-xl font-bold text-slate-100 tracking-wide mb-2">Access Restricted</h2>
        <p className="text-slate-400 text-sm mb-6 leading-relaxed">
          Your current security role (<span className="text-amber-400 font-mono font-medium">{user?.role || 'UNAUTHENTICATED'}</span>) does not have sufficient clearance to access this module. Contact a System Administrator to request elevated privileges.
        </p>
        <Link
          to="/dashboard"
          className="inline-flex items-center px-4 py-2 text-sm font-medium text-slate-100 bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-lg transition-colors"
        >
          Return to Dashboard
        </Link>
      </div>
    );
  }

  return <>{children}</>;
};

