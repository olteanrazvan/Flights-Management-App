# GhostFlights Frontend - React TypeScript + Material-UI

A modern flight booking application built with React, TypeScript, and Material-UI, featuring a comprehensive flight management system for both clients and administrators.

## 🚀 Features

### For Clients
- **User Authentication**: Secure login/register with JWT tokens
- **Flight Search**: Advanced search with filters (origin, destination, date, passengers)
- **Flight Booking**: Complete booking workflow with seat selection
- **Ticket Management**: View, confirm, cancel tickets with PDF download
- **Real-time Notifications**: Booking confirmations and flight updates
- **Responsive Dashboard**: Personal flight statistics and quick actions

### For Administrators
- **Flight Management**: CRUD operations for flights
- **Admin Dashboard**: Comprehensive analytics and statistics
- **User Management**: View and manage user accounts
- **Booking Overview**: Monitor all flight bookings
- **System Analytics**: Occupancy rates, revenue tracking

## 🛠️ Technology Stack

- **Frontend Framework**: React 18 with TypeScript
- **UI Library**: Material-UI (MUI) v5
- **State Management**: React Context + Hooks
- **Routing**: React Router v6
- **HTTP Client**: Axios with interceptors
- **Date Handling**: Day.js
- **Form Validation**: Built-in validation
- **Icons**: Material-UI Icons

## 📦 Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd ghost-flights-frontend
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Environment Setup**
   Create a `.env` file in the root directory:
   ```env
   REACT_APP_API_URL=http://localhost:8080/api
   ```

4. **Start the development server**
   ```bash
   npm start
   ```

5. **Build for production**
   ```bash
   npm run build
   ```

## 🏗️ Project Structure

```
src/
├── components/          # Reusable UI components
│   ├── auth/           # Authentication components
│   ├── flights/        # Flight-related components
│   ├── tickets/        # Ticket management components
│   ├── notifications/  # Notification system
│   └── layout/         # Layout components
├── pages/              # Page components
│   ├── auth/          # Login/Register pages
│   ├── flights/       # Flight pages
│   ├── tickets/       # Ticket pages
│   └── dashboard/     # Dashboard pages
├── services/          # API service layer
├── types/             # TypeScript type definitions
├── context/           # React Context providers
├── hooks/             # Custom React hooks
├── utils/             # Utility functions
└── theme/             # Material-UI theme configuration
```

## 🎨 Design System

### Color Palette
- **Primary**: Aviation Blue (#1976d2)
- **Secondary**: Sunset Orange (#ff6b35)
- **Ghost**: Dark Blue (#2c2c54)
- **Background**: Light Gray (#f8f9fa)

### Typography
- **Font Family**: Inter, Roboto, Helvetica, Arial
- **Headings**: Bold weights with proper hierarchy
- **Body Text**: Optimized for readability

### Components
- **Cards**: Elevated with hover effects and rounded corners
- **Buttons**: Modern styling with appropriate elevation
- **Forms**: Clean inputs with proper validation states
- **Navigation**: Responsive with role-based menu items

## 🔐 Authentication & Authorization

### JWT Token Management
- Automatic token refresh
- Secure storage in localStorage
- Request interceptors for API calls
- Protected route components

### Role-Based Access Control
- **CLIENT**: Access to booking and personal tickets
- **ADMIN**: Full system access including flight management

### Protected Routes
```tsx
<AuthGuard requireAdmin>
  <AdminComponent />
</AuthGuard>
```

## 📱 Responsive Design

- **Mobile First**: Optimized for mobile devices
- **Breakpoints**: 
  - xs: 0px
  - sm: 600px
  - md: 900px
  - lg: 1200px
  - xl: 1536px

## 🔄 Data Flow

### State Management
1. **AuthContext**: User authentication state
2. **NotificationContext**: Real-time notifications
3. **Local State**: Component-specific state with hooks

### API Integration
```typescript
// Service layer example
export const flightService = {
  async searchFlights(params: FlightSearchParams): Promise<Flight[]> {
    const response = await api.get('/flights/search', { params });
    return response.data;
  }
};
```

## 🧪 Key Features Implementation

### Flight Search
- Real-time availability checking
- Advanced filtering options
- Responsive results display
- Booking flow integration

### Ticket Management
- Status-based organization (Reserved, Confirmed, Cancelled)
- PDF generation and download
- Bulk operations for admins
- Real-time status updates

### Admin Panel
- Comprehensive flight CRUD operations
- System analytics and reporting
- User management capabilities
- Booking oversight tools

## 🚀 Deployment

### Environment Variables
```env
REACT_APP_API_URL=your-backend-url
REACT_APP_VERSION=1.0.0
```

### Build Process
```bash
npm run build
# Generates optimized production build in /build
```

### Backend Integration
Ensure your Spring Boot backend is running on the configured API URL with CORS enabled for your frontend domain.

## 🔧 Development Guidelines

### Code Style
- Use TypeScript for type safety
- Follow React functional component patterns
- Implement proper error boundaries
- Use custom hooks for business logic

### Performance Optimizations
- Code splitting with React.lazy()
- Memoization for expensive calculations
- Optimized bundle size
- Efficient re-rendering patterns

## 📞 API Endpoints

The frontend integrates with these main backend endpoints:

- **Auth**: `/api/auth/*`
- **Users**: `/api/users/*`
- **Flights**: `/api/flights/*`
- **Tickets**: `/api/tickets/*`
- **Notifications**: `/api/notifications/*`

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🎯 Future Enhancements

- [ ] Real-time flight tracking
- [ ] Mobile app development
- [ ] Advanced analytics dashboard
- [ ] Multi-language support
- [ ] Integration with payment gateways
- [ ] Social media authentication
- [ ] Push notifications
- [ ] Offline support with PWA