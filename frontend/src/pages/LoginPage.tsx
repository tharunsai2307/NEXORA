import React, { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Building2, Shield, Eye, EyeOff, Lock, Mail, ArrowRight, AlertCircle } from 'lucide-react';

export const LoginPage: React.FC = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const from = (location.state as { from?: { pathname: string } })?.from?.pathname || '/dashboard';

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);

    try {
      await login({ email, password });
      navigate(from, { replace: true });
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || 'Authentication failed. Please verify credentials.';
      setError(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  const setDemoCredentials = (demoEmail: string) => {
    setEmail(demoEmail);
    setPassword('NexoraAdmin2026!');
    setError(null);
  };

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col justify-center items-center px-4 sm:px-6 lg:px-8 relative overflow-hidden">
      {/* Background industrial grid glow */}
      <div className="absolute inset-0 bg-[linear-gradient(to_right,#1e293b15_1px,transparent_1px),linear-gradient(to_bottom,#1e293b15_1px,transparent_1px)] bg-[size:4rem_4rem] pointer-events-none" />
      <div className="absolute top-1/4 -left-32 w-96 h-96 bg-emerald-500/10 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-1/4 -right-32 w-96 h-96 bg-cyan-500/10 rounded-full blur-3xl pointer-events-none" />

      <div className="w-full max-w-md space-y-8 relative z-10">
        {/* Header Branding */}
        <div className="text-center space-y-3">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-br from-emerald-500/20 to-cyan-500/20 border border-emerald-500/30 shadow-lg shadow-emerald-500/10">
            <Building2 className="w-9 h-9 text-emerald-400" />
          </div>
          <h1 className="text-3xl font-extrabold tracking-tight text-slate-100 flex items-center justify-center gap-2">
            NEXORA
            <span className="text-xs px-2 py-0.5 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 font-mono font-normal">
              v2.0
            </span>
          </h1>
          <p className="text-xs uppercase tracking-widest text-slate-400 font-mono">
            Intelligence Beyond Infrastructure
          </p>
        </div>

        {/* Card */}
        <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-8 shadow-2xl backdrop-blur-xl">
          <div className="mb-6">
            <h2 className="text-lg font-semibold text-slate-100">Sign in to control platform</h2>
            <p className="text-xs text-slate-400 mt-1">
              Enter your corporate credentials to access digital twin telemetry and controls.
            </p>
          </div>

          {error && (
            <div className="mb-6 p-3 rounded-lg bg-red-500/10 border border-red-500/30 flex items-start gap-2.5 text-xs text-red-300">
              <AlertCircle className="w-4 h-4 text-red-400 shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label htmlFor="login-email" className="block text-xs font-medium text-slate-300 mb-1">Corporate Email</label>
              <div className="relative">
                <Mail className="w-4 h-4 text-slate-500 absolute left-3 top-3 pointer-events-none" />
                <input
                  id="login-email"
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="operator@nexora.com"
                  className="w-full pl-9 pr-4 py-2.5 bg-slate-950/60 border border-slate-800 focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 rounded-lg text-sm text-slate-100 placeholder-slate-600 transition-colors outline-none"
                />
              </div>
            </div>

            <div>
              <label htmlFor="login-password" className="block text-xs font-medium text-slate-300 mb-1">Access Password</label>
              <div className="relative">
                <Lock className="w-4 h-4 text-slate-500 absolute left-3 top-3 pointer-events-none" />
                <input
                  id="login-password"
                  type={showPassword ? 'text' : 'password'}
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••••••"
                  className="w-full pl-9 pr-10 py-2.5 bg-slate-950/60 border border-slate-800 focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500 rounded-lg text-sm text-slate-100 placeholder-slate-600 transition-colors outline-none"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-2.5 text-slate-500 hover:text-slate-300 transition-colors"
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full py-2.5 px-4 bg-emerald-600 hover:bg-emerald-500 disabled:bg-emerald-600/50 text-white rounded-lg text-sm font-medium transition-all shadow-lg shadow-emerald-600/20 flex items-center justify-center gap-2 group mt-2"
            >
              {isSubmitting ? (
                <>
                  <div className="w-4 h-4 border-2 border-white/20 border-t-white rounded-full animate-spin" />
                  <span>Authenticating...</span>
                </>
              ) : (
                <>
                  <span>Authenticate Session</span>
                  <ArrowRight className="w-4 h-4 group-hover:translate-x-0.5 transition-transform" />
                </>
              )}
            </button>
          </form>

          {/* Quick Demo Fill Buttons */}
          <div className="mt-6 pt-6 border-t border-slate-800/80">
            <div className="flex items-center gap-1.5 text-xs text-slate-400 font-mono mb-2.5">
              <Shield className="w-3.5 h-3.5 text-cyan-400" />
              <span>Select Preconfigured Demo Role</span>
            </div>
            <div className="grid grid-cols-3 gap-2">
              <button
                type="button"
                onClick={() => setDemoCredentials('admin@nexora.com')}
                className="px-2 py-1.5 text-xs font-mono bg-slate-950 hover:bg-slate-800 border border-slate-800 rounded text-slate-300 hover:text-emerald-400 transition-colors text-center"
              >
                Admin
              </button>
              <button
                type="button"
                onClick={() => setDemoCredentials('manager@nexora.com')}
                className="px-2 py-1.5 text-xs font-mono bg-slate-950 hover:bg-slate-800 border border-slate-800 rounded text-slate-300 hover:text-emerald-400 transition-colors text-center"
              >
                Manager
              </button>
              <button
                type="button"
                onClick={() => setDemoCredentials('safety@nexora.com')}
                className="px-2 py-1.5 text-xs font-mono bg-slate-950 hover:bg-slate-800 border border-slate-800 rounded text-slate-300 hover:text-emerald-400 transition-colors text-center"
              >
                Safety Off.
              </button>
            </div>
          </div>
        </div>

        {/* Footer Registration Link */}
        <div className="text-center text-xs text-slate-400">
          <span>Need a new operator account? </span>
          <Link to="/register" className="text-emerald-400 hover:text-emerald-300 font-medium transition-colors">
            Register as Building Manager
          </Link>
        </div>
      </div>
    </div>
  );
};

