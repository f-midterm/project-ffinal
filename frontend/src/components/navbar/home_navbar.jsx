import React, { useEffect, useRef, useState, useCallback, useMemo } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import NavItem from './nav_items';
import { logout, getUsername } from '../../api';
import { useAuth } from '../../hooks/useAuth';
import { FiUser, FiLogOut, FiGrid } from 'react-icons/fi';

function HomeNavbar({ isAuthenticated, onAuthChange }) {
  const navLinks = useMemo(
    () => [
      { to: '/application', label: 'Application' },
      { to: '/partner', label: 'Partner' },
      { to: '/service', label: 'Service' },
      { to: '/contact', label: 'Contact' },
    ],
    []
  );

  const [hidden, setHidden] = useState(false);
  const lastScroll = useRef(0);
  const ticking = useRef(false);
  const [scrolled, setScrolled] = useState(false);
  const [isDropdownOpen, setDropdownOpen] = useState(false);
  const navigate = useNavigate();

  const { isAdmin, user } = useAuth();
  const username = getUsername();
  const dropdownRef = useRef(null);

  useEffect(() => {
    const onScroll = () => {
      const current = window.scrollY || window.pageYOffset;
      setScrolled(current > 10);
      if (!ticking.current) {
        window.requestAnimationFrame(() => {
          if (current > lastScroll.current && current > 80) {
            setHidden(true);
          } else {
            setHidden(false);
          }
          lastScroll.current = current;
          ticking.current = false;
        });
        ticking.current = true;
      }
    };

    window.addEventListener('scroll', onScroll, { passive: true });
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  const handleLogout = useCallback(() => {
    logout();
    setDropdownOpen(false);
    onAuthChange();
    navigate('/');
  }, [navigate, onAuthChange]);

  const handleDropdownToggle = () => {
    setDropdownOpen((prev) => !prev);
  };

  const handleClickOutside = useCallback(
    (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setDropdownOpen(false);
      }
    },
    [dropdownRef]
  );

  useEffect(() => {
    if (isDropdownOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    } else {
      document.removeEventListener('mousedown', handleClickOutside);
    }
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [isDropdownOpen, handleClickOutside]);

  return (
    <div
      className={`fixed top-0 left-0 right-0 z-50 transition-transform duration-300 ${
        hidden ? '-translate-y-full' : 'translate-y-0'
      }`}
    >
      <div
        className={`w-full transition-colors duration-300 shadow-md ${
          scrolled ? 'bg-white' : 'bg-white/75'
        }`}
      >
        <div className="flex px-24 py-4 justify-center">
          <div className="flex items-center gap-24 gap-32">
            <Link to="/" className="text-4xl font-semibold logo">
              BeLiv
            </Link>

            <ul className="flex gap-6 items-center">
              {navLinks.map((link) => (
                <li key={link.to}>
                  <NavItem to={link.to}>{link.label}</NavItem>
                </li>
              ))}
            </ul>

            <div className="ml-auto flex items-center gap-4 relative" ref={dropdownRef}>
              {isAuthenticated ? (
                <>
                  <button
                    onClick={handleDropdownToggle}
                    className="btn rounded-full p-3 border-2 border-[#0076D4] hover:shadow-md hover:translate-y-[-0.5px]"
                  >
                    <FiUser size={24} className="text-[#0076D4]" />
                  </button>
                  <span className="font-medium text-gray-700">{username}</span>

                  {isDropdownOpen && (
                    <div className="absolute top-full right-0 mt-2 w-48 bg-white rounded-md shadow-lg z-10">
                      <ul className="py-1">
                        <li>
                          <Link
                            to={`/user/${user?.id}`}
                            className="flex items-center px-4 py-2 text-gray-800 hover:bg-gray-100"
                            onClick={() => setDropdownOpen(false)}
                          >
                            <FiUser className="mr-2" />
                            Profile
                          </Link>
                        </li>
                        {isAdmin && (
                          <li>
                            <Link
                              to="/admin"
                              className="flex items-center px-4 py-2 text-gray-800 hover:bg-gray-100"
                              onClick={() => setDropdownOpen(false)}
                            >
                              <FiGrid className="mr-2" />
                              Dashboard
                            </Link>
                          </li>
                        )}
                        <li>
                          <button
                            onClick={handleLogout}
                            className="flex items-center w-full px-4 py-2 text-left text-gray-800 hover:bg-gray-100"
                          >
                            <FiLogOut className="mr-2" />
                            Logout
                          </button>
                        </li>
                      </ul>
                    </div>
                  )}
                </>
              ) : (
                <>
                  <Link
                    to="/signup"
                    className="btn-primary rounded-full text-white text-lg font-medium px-8 py-3 bg-gradient-to-r from-[#0076D4] to-[#303841] shadow-md hover:translate-y-[-2px] transition-transform"
                  >
                    Get Started
                  </Link>
                  <Link
                    to="/login"
                    className="btn-secondary rounded-full text-lg font-medium px-8 py-3 text-transparent bg-clip-text border-2 hover:translate-y-[-2px] transition-transform border-[#0076D4] bg-gradient-to-r from-[#0076D4] to-[#303841]"
                  >
                    Login
                  </Link>
                </>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default HomeNavbar;